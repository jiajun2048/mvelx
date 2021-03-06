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

package org.mvel2.optimizers.dynamic;

import org.mvel2.compiler.Accessor;

/**
 * 动态访问器，用于支持使用多个访问器在运行中切换实现的处理式
 * 即优化时可以采用访问器2,在安全访问时也可以使用访问器1
 */
public interface DynamicAccessor extends Accessor {
    /** 反优化，表示在后面的处理中将不再使用原来的优化器.同时可以解释相应的优化类资源 */
    void deoptimize();
}
