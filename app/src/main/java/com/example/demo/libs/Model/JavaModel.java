package com.example.demo.libs.Model;

public class JavaModel {
    public enum BoxType
    {
        UNUSED(0);

        public static final int SIZE = java.lang.Integer.SIZE;

        private int intValue;
        private static java.util.HashMap<Integer, BoxType> mappings;
        private static java.util.HashMap<Integer, BoxType> getMappings()
        {
            if (mappings == null)
            {
                synchronized (BoxType.class)
                {
                    if (mappings == null)
                    {
                        mappings = new java.util.HashMap<Integer, BoxType>();
                    }
                }
            }
            return mappings;
        }

        private BoxType(int value)
        {
            intValue = value;
            getMappings().put(value, this);
        }

        public int getValue()
        {
            return intValue;
        }

        public static BoxType forValue(int value)
        {
            return getMappings().get(value);
        }
    }
}
