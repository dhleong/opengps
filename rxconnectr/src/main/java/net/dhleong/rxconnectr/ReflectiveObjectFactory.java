package net.dhleong.rxconnectr;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;

/**
 * @author dhleong
 */
class ReflectiveObjectFactory<T> implements ObjectFactory<T> {

    private final Class<T> type;
    private int dataId;

    private List<FieldSpec> spec;

    ReflectiveObjectFactory(Class<T> type) {
        this.type = type;
    }

    @Override
    public T create(RecvSimObjectData data) {
        if (dataId != data.getDefineID()) {
            throw new IllegalArgumentException("Data define id mismatch: "
                + "received " + data.getDefineID()
                + "; expected " + dataId);
        }

        if (spec == null) {
            throw new IllegalStateException("Not initialized; call bindToDefinition first");
        }

        T result = newInstance();

        for (int i=0, fields=spec.size(); i < fields; i++) {
            FieldSpec field = spec.get(i);
            field.setOn(result, data);
        }

        return result;
    }

    @Override
    public void bindToDataDefinition(SimConnect connect, int dataId) throws IOException {
        final List<FieldSpec> existing = spec;
        final List<FieldSpec> theSpec;
        if (existing == null) {
            theSpec = spec = new ArrayList<>();
            initSpec(type, theSpec);
        } else {
            theSpec = existing;
        }

        this.dataId = dataId;
        for (FieldSpec field : theSpec) {
            connect.addToDataDefinition(dataId, field.datumName, field.unit, field.dataType);
        }
    }

    @Override
    public int getDataId() {
        return dataId;
    }

    private T newInstance() {
        try {
            return type.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static void initSpec(Class<?> klass, List<FieldSpec> spec) {
        final Field[] fields = klass.getDeclaredFields();
        for (Field field : fields) {
            FieldSpec fieldSpec = FieldSpec.from(field);
            if (fieldSpec != null) {
                spec.add(fieldSpec);
            }
        }

        final Class<?> superType = klass.getSuperclass();
        if (superType != Object.class) {
            initSpec(superType, spec);
        }
    }

    static class FieldSpec {

        static final int TYPE_INT = 0;
        static final int TYPE_LONG = 1;
        static final int TYPE_FLOAT = 2;
        static final int TYPE_DOUBLE = 3;
        static final int TYPE_BOOL = 4;

        final String datumName;
        final String unit;
        final SimConnectDataType dataType;
        final Field field;

        final int actualType;

        FieldSpec(String datumName, String unit, SimConnectDataType dataType, Field field, int actualType) {
            this.datumName = datumName;
            this.unit = unit;
            this.dataType = dataType;
            this.field = field;
            this.actualType = actualType;
        }

        void setOn(Object obj, RecvSimObjectData data) {
            try {
                switch (actualType) {
                case TYPE_INT:
                    field.setInt(obj, data.getDataInt32());
                    break;
                case TYPE_LONG:
                    field.setLong(obj, data.getDataInt64());
                    break;
                case TYPE_FLOAT:
                    field.setFloat(obj, data.getDataFloat32());
                    break;
                case TYPE_DOUBLE:
                    field.setDouble(obj, data.getDataFloat64());
                    break;
                case TYPE_BOOL:
                    field.setBoolean(obj, data.getDataInt32() != 0);
                    break;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public static FieldSpec from(Field field) {
            final ConnectrField spec = field.getAnnotation(ConnectrField.class);
            if (spec == null) return null;

            // make sure we can r/w
            field.setAccessible(true);

            final int actualType;
            final SimConnectDataType dataType;
            final Class<?> fieldType = field.getType();
            if (fieldType == int.class) {
                actualType = TYPE_INT;
                dataType = SimConnectDataType.INT32;
            } else if (fieldType == long.class) {
                actualType = TYPE_LONG;
                dataType = SimConnectDataType.INT64;
            } else if (fieldType == float.class) {
                actualType = TYPE_FLOAT;
                dataType = SimConnectDataType.FLOAT32;
            } else if (fieldType == double.class) {
                actualType = TYPE_DOUBLE;
                dataType = SimConnectDataType.FLOAT64;
            } else if (fieldType == boolean.class) {
                actualType = TYPE_BOOL;
                dataType = SimConnectDataType.INT32;
            } else {
                throw new IllegalArgumentException("Unexpected field type " + fieldType + " on " + field);
            }

            return new FieldSpec(spec.datumName(), spec.unit(), dataType, field, actualType);
        }
    }
}
