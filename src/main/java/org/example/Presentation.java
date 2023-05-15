package org.example;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.lang.System.out;

public class Presentation {






    String hello = "My name is Anton";








    /**
     * <h1>Why debug?</h1>
     * <ul style="font-size:18px"/>
     * <li>Find and fix bugs!</li>
     * <li>Analyze code behaviour</li>
     * <li>Change code behaviour (at runtime)</li>
     * <li>Analyze memory issues</li>
     * </ul>
     */
    int debug;













    /**
     * <h1>Change behavior at runtime</h1>
     * <ul style="font-size:18px"/>
     * <li>HotSwap</li>
     * <li>Evaluate expression / fragment</li>
     * <li>Reset frame</li>
     * <li>Force return & throw</li>
     * </ul>
     */
    ChangeAtRuntime hotswap;






















    /**
     * <h1>Debugging stream chains with Lambdas</h1>
     * <ul style="font-size:18px"/>
     * <li>Breakpoints for lambdas</li>
     * <li>Run to cursor</li>
     * <li>(Smart) Step into lambdas</li>
     * <li>Trace current stream chain</li>
     * </ul>
     */
    public static class Lambdas {
        public static void main(String[] args) {
            List<String> reader = List.of("aaaa", "bbbbb", "cccc", "dddd");
            List<String> lines = reader.stream().filter(l -> l.length() > 3).peek(l -> out.println(l)).filter(l -> !l.startsWith("a")).toList();
            out.println(lines);
        }
    }



















    /**
     * <h1>Method breakpoints</h1>
     * <ul style="font-size:18px"/>
     * <li>Good for interface methods</li>
     * <li>"Catch any invocation!"</li>
     * </ul>
     */
    public static class MethodBreakpoints {
        public static void main(String[] args) {
            BaseInterface i = Somewhere.getObject();
            out.println(i.boo());
        }

        interface BaseInterface {
            String foo(); //unused?
            String boo();
        }

        //region somewhere
        static class Somewhere {
            static BaseInterface getObject() {
                doSomething();

                class Clazz implements BaseInterface {
                    @Override
                    public String foo() {return "Clazz.foo";}

                    @Override
                    public String boo() {return "Clazz.boo";}
                }
                return new Clazz();
            }

            //region something
            private static void doSomething() {
                try {
                    //region hidden
                    BaseInterface my = new BaseInterface() {
                        @Override
                        public String foo() {
                            out.println("Hello from Somewhere!");
                            return null;
                        }

                        @Override
                        public String boo() {
                            return null;
                        }
                    };
                    my.getClass().getMethod("foo").invoke(my);
                    //endregion
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            //endregion
        }
        //endregion
    }














    /**
     * <h1>Breakpoints</h1>
     * <ul style="font-size:18px"/>
     * <li>Non-suspending breakpoints</li>
     * <li>Dependent breakpoints</li>
     * <li>Filters & intentions</li>
     * </ul>
     */
    public static class Breakpoints {










        public static class Cache {

            static Cache instance;

            static Cache getInstance(int i) {
                if (instance == null) {
                    instance = new Cache();
                }
                return instance;
            }

            public static void main(String[] args) {
                for (int i = 0; i < 10; i++) {
                    System.out.println(getInstance(i));
                }
            }
        }














        static public class ComplexWorkflow {

            public static void main(String[] args) {
                ComplexWorkflow workflow = new ComplexWorkflow();
                Object o = workflow.process("1234");
                out.println(o);
            }

            public User process(String id){
                step1(id);
                return new User(id);
            }

            private void step1(String id) {
                step2(id);
            }

            private void step2(String id) {
                step3(id);
                step4(id);
            }

            private void step3(String id) {
                output(id);
            }

            private void step4(String id) {
                action(id);
            }

            void action(String id){
                output(id);
            }

            private void output(String id) {
                System.err.println(id);
            }
        }
    }




















    /**
     * @see org.example.ConcurrencyTest
     */
    int concurrencyTest;













}
