package cn.procsl.ping.processor.v3.web.parser;


//class RequestControllerImpl implements TypeDescriptor {
//
//    final TypeElement target;
//
//    final List<FieldDescriptor> fieldDescriptors;
//
//    final List<MethodDescriptor> methodDescriptors;
//
//    RequestControllerImpl(TypeElement target,
//                          List<FieldDescriptor> fieldDescriptors,
//                          List<MethodDescriptor> methodDescriptors) {
//        this.target = target;
//        this.fieldDescriptors = fieldDescriptors;
//        this.methodDescriptors = methodDescriptors;
//    }
//
//
//    @Override
//    public String getPackageName() {
//        return target.getQualifiedName().toString();
//    }
//
//    @Override
//    public String getClassName() {
//        return target.getSimpleName().toString();
//    }
//
//    @Override
//    public List<FieldDescriptor> getFields() {
//        return Collections.unmodifiableList(this.fieldDescriptors);
//    }
//
//    @Override
//    public List<MethodDescriptor> getMethods() {
//        return Collections.unmodifiableList(this.methodDescriptors);
//    }
//}