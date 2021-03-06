/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mvel2.optimizers.impl.asm;

import org.mvel2.asm.MethodVisitor;
import org.mvel2.integration.VariableResolverFactory;

/**
 * 用于描述一个可以自定义如何产生自定义字节码的标记接口,当一个propertyHandler被使用时,并且此处理器实现了此接口,则就会在asm优化时
 * 生成相应的自定义字节码来代替原来的反射访问
 * A {@link org.mvel2.integration.PropertyHandler} that implements this class advertises the to the
 * {@link ASMAccessorOptimizer} that it is able to generate bytecode for it's custom resolvers.<br/>
 * <br/>
 * The two methods defined by this interface (one for get, and one for set accessors) are passed an
 * ASM MethodVistor object.  It is to be assumed by the implementor that the JIT has already produced
 * the necessary bytecode up until this point.  The implementor most only implement the necessary bytecode
 * instructions to retrieve the property, leaving the resultant value on the stack.  <b>DO NOT</b> implement
 * bytecode that calls a return instruction such as ARETURN.  This will be done automatically by the JIT.<br/>
 * <br/>
 * See the following example:<br/>
 * <pre><code>
 *   public void produceBytecodeGet(MethodVisitor mv, String propertyName, VariableResolverFactory variableResolverFactory) {
 * mv.visitTypeInsn(CHECKCAST, "org/mvel/tests/main/res/SampleBean");
 * mv.visitLdcInsn(propertyName);
 * mv.visitMethodInsn(INVOKEVIRTUAL, "org/mvel/tests/main/res/SampleBean", "getProperty", "(Ljava/lang/String;)Ljava/lang/Object;");
 * }
 * </code></pre><br/>
 * This example (correctly) presumes that the property of interest exists on the stack, and simply performs the necessary
 * CHECKCAST, and call to the needed method in the accessor to translate the property call. <br/>
 * <br/>
 * This is taken from a working example which you can examine in the MVEL unit tests.
 * The class is: org.mvel.tests.main.res.SampleBeanAccessor
 */
public interface ProducesBytecode {
    /** 在进行get调用时产生相应的字节码 */
    void produceBytecodeGet(MethodVisitor mv, String propertyName, VariableResolverFactory factory);

    /** 在进行set调用时产生相应的字节码 */
    void produceBytecodePut(MethodVisitor mv, String propertyName, VariableResolverFactory factory);
}
