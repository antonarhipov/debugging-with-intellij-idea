package org.example;

import java.lang.reflect.InvocationTargetException;

public class MethodBreakpoint {
    public static void main(String[] args) {
        MyInterface i = SomeSource.getSomething();
        System.out.println(i.boo());
    }
}

interface MyInterface {
    String foo();
    String boo();
}













class SomeSource {
    static MyInterface getSomething(){
        doSomething();

        return new MyInterface() {
            //region impl
            @Override
            public String foo() {
                return null;
            }

            @Override
            public String boo() {

                return null;
            }
            //endregion
        };
    }

    //region hidden
    private static void doSomething() {
        try {
            //region instance
            MyInterface my = new MyInterface() {
                @Override
                public String foo() {
                    return null;
                }

                @Override
                public String boo() {
                    return null;
                }
            };
            //endregion
            //region hidden
            my.getClass().getMethod("foo").invoke(my);
            //endregion
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion
}



