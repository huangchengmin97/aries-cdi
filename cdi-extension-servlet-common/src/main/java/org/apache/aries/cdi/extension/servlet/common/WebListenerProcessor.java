/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.aries.cdi.extension.servlet.common;

import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.servlet.annotation.WebListener;

import org.apache.aries.cdi.extra.propertytypes.HttpWhiteboardContextSelect;
import org.apache.aries.cdi.extra.propertytypes.HttpWhiteboardListener;
import org.apache.aries.cdi.extra.propertytypes.ServiceDescription;
import org.apache.aries.cdi.spi.configuration.Configuration;
import org.osgi.service.cdi.annotations.Service;

public class WebListenerProcessor {

	/**
	 * Call this method from an observer defined as:
	 * <pre>
	 * &lt;X&gt; void webListener(@Observes @WithAnnotations(WebListener.class) ProcessAnnotatedType&lt;X&gt; pat) {
	 *   new WebListenerProcessor().process(configuration, pat);
	 * }
	 * </pre>
	 * @param <X>
	 * @param pat
	 */
	public <X> void process(
		Configuration configuration, ProcessAnnotatedType<X> pat) {

		final AnnotatedType<X> annotatedType = pat.getAnnotatedType();

		WebListener webListener = annotatedType.getAnnotation(WebListener.class);

		final Set<Annotation> annotationsToAdd = new HashSet<>();

		if (!annotatedType.isAnnotationPresent(Service.class)) {
			List<Class<?>> listenerTypes = new ArrayList<>();

			Class<X> javaClass = annotatedType.getJavaClass();

			if (javax.servlet.ServletContextListener.class.isAssignableFrom(javaClass)) {
				listenerTypes.add(javax.servlet.ServletContextListener.class);
			}
			if (javax.servlet.ServletContextAttributeListener.class.isAssignableFrom(javaClass)) {
				listenerTypes.add(javax.servlet.ServletContextAttributeListener.class);
			}
			if (javax.servlet.ServletRequestListener.class.isAssignableFrom(javaClass)) {
				listenerTypes.add(javax.servlet.ServletRequestListener.class);
			}
			if (javax.servlet.ServletRequestAttributeListener.class.isAssignableFrom(javaClass)) {
				listenerTypes.add(javax.servlet.ServletRequestAttributeListener.class);
			}
			if (javax.servlet.http.HttpSessionListener.class.isAssignableFrom(javaClass)) {
				listenerTypes.add(javax.servlet.http.HttpSessionListener.class);
			}
			if (javax.servlet.http.HttpSessionAttributeListener.class.isAssignableFrom(javaClass)) {
				listenerTypes.add(javax.servlet.http.HttpSessionAttributeListener.class);
			}
			if (javax.servlet.http.HttpSessionIdListener.class.isAssignableFrom(javaClass)) {
				listenerTypes.add(javax.servlet.http.HttpSessionIdListener.class);
			}

			annotationsToAdd.add(Service.Literal.of(listenerTypes.toArray(new Class<?>[0])));
		}

		if(!annotatedType.isAnnotationPresent(HttpWhiteboardContextSelect.class)) {
			annotationsToAdd.add(HttpWhiteboardContextSelect.Literal.of((String)configuration.get(HTTP_WHITEBOARD_CONTEXT_SELECT)));
		}

		annotationsToAdd.add(HttpWhiteboardListener.Literal.INSTANCE);

		if (!webListener.value().isEmpty()) {
			annotationsToAdd.add(ServiceDescription.Literal.of(webListener.value()));
		}

		if (!annotationsToAdd.isEmpty()) {
			annotationsToAdd.forEach(pat.configureAnnotatedType()::add);
		}
	}

}