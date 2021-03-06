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

package org.mvel2.ast;

import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.VariableResolverFactory;

/**
 * 表示一个在当前上下文中具体指定参数顺序的var节点,用于对象声明
 * @author Christopher Brock
 */
public class IndexedDeclTypedVarNode extends ASTNode implements Assignment {
    /** 当前声明变量在上下文参数中的位置(入参或本地变量) */
    private int register;

    public IndexedDeclTypedVarNode(int register, int start, int offset, Class type, ParserContext pCtx) {
        super(pCtx);
        this.egressType = type;
        this.start = start;
        this.offset = offset;
        this.register = register;
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        //因为之前指定了相应的下标位置,因此这里直接进行声明即可
        factory.createIndexedVariable(register, null, egressType);
        return ctx;
    }

    public String getAssignmentVar() {
        return null;
    }

    public char[] getExpression() {
        return new char[0];
    }

    /** 当前节点为声明节点 */
    public boolean isAssignment() {
        return true;
    }

    /** 当前节点为新声明节点(并未赋值) */
    public boolean isNewDeclaration() {
        return true;
    }

    public void setValueStatement(ExecutableStatement stmt) {
        throw new RuntimeException("illegal operation");
    }
}