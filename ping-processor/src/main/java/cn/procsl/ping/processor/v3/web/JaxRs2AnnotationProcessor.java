package cn.procsl.ping.processor.v3.web;

/**
 * 生成 SpringController 类
 */
//@AutoService(Processor.class)
//@SuppressWarnings("unused")
//public class JaxRs2AnnotationProcessor extends AbstractProcessor {
//
//    final ServiceElementVisitor visitor = new ServiceElementVisitor();
//    final Collection<JavaSourceGenerator> generators;
//
//    public JaxRs2AnnotationProcessor() {
//        generators = Arrays.asList(new AbstractJavaSourceGenerator(), new ParameterGenerator(), new ReturnGenerator());
//    }
//
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        SimpleEnvironment env = new SimpleEnvironment(this.processingEnv, roundEnv);
//
//        ArrayList<Descriptor> result = new ArrayList<>();
//        annotations.stream().map(item -> item.accept(visitor, env)).filter(Objects::nonNull).forEach(item -> {
//            item.accept(this.typeDescriptorIterable);
//        });
//
//        for (JavaSourceGenerator generator : generators) {
//            try {
//                generator.generated(list, env);
//            } catch (java.io.IOException e) {
//                env.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
//            }
//        }
//        return true;
//    }
//
//
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        return Collections.singleton(Path.class.getName());
//    }
//
//    private static class ServiceElementVisitor implements ElementVisitor<TypeDescriptor, ProcessorEnvironment> {
//
//        final ServiceElementParser parser = new ServiceElementParser();
//
//        @Override
//        public TypeDescriptor visit(Element e, ProcessorEnvironment processorEnvironment) {
//            return null;
//        }
//
//        @Override
//        public TypeDescriptor visitPackage(PackageElement e, ProcessorEnvironment processorEnvironment) {
//            return null;
//        }
//
//        @Override
//        public TypeDescriptor visitType(TypeElement e, ProcessorEnvironment processorEnvironment) {
//            return parser.parse(e, processorEnvironment);
//        }
//
//        @Override
//        public TypeDescriptor visitVariable(VariableElement e, ProcessorEnvironment processorEnvironment) {
//            return null;
//        }
//
//        @Override
//        public TypeDescriptor visitExecutable(ExecutableElement e, ProcessorEnvironment processorEnvironment) {
//            return null;
//        }
//
//        @Override
//        public TypeDescriptor visitTypeParameter(TypeParameterElement e, ProcessorEnvironment processorEnvironment) {
//            return null;
//        }
//
//        @Override
//        public TypeDescriptor visitUnknown(Element e, ProcessorEnvironment processorEnvironment) {
//            return null;
//        }
//    }
//
//}
