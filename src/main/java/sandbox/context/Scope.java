/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sandbox.context;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A contract for objects that can be scoped (e.g. in a new thread).
 *
 * @see ContextSnapshot
 * @since 1.0.0
 */
public interface Scope extends AutoCloseable {
    /**
     * Opens the scope and makes the propagation context current.
     *
     * @param propagationContext propagation context to make current
     * @return itself
     */
    Scope open(ContextSnapshot propagationContext);

    @Override
    void close();

    /**
     * Scope that contains a list of scopes.
     */
    class CompositeScope {
        private final List<ScopeProvider> scopes;

        public CompositeScope(List<ScopeProvider> scopes) {
            this.scopes = scopes;
        }

        public Scope open(ContextSnapshot contextSnapshot) {
            return group(contextSnapshot).open(contextSnapshot);
        }

        private Scope group(ContextSnapshot contextSnapshot) {
            return new ScopeHolder(this.scopes.stream().map(scope -> scope.apply(contextSnapshot)).collect(Collectors.toList()));
        }

        static class ScopeHolder implements Scope {

            private final List<Scope> scopes;

            ScopeHolder(List<Scope> scopes) {
                this.scopes = scopes;
            }

            @Override
            public Scope open(ContextSnapshot propagationContext) {
                this.scopes.forEach(scope -> scope.open(propagationContext));
                return this;
            }

            @Override
            public void close() {
                this.scopes.forEach(Scope::close);
            }
        }
    }
}
