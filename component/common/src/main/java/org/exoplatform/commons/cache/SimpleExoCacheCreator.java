/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.commons.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.services.cache.impl.infinispan.ExoCacheCreator;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;

/**
 * Gives more flexibility in GateIn cache usage.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SimpleExoCacheCreator implements ExoCacheCreator {

  @Override
  public ExoCache<Serializable, Object> create(ExoCacheConfig config, ConfigurationBuilder confBuilder, Callable<Cache<Serializable, Object>> cacheGetter) throws ExoCacheInitException {
    ExoCache<Serializable, Object> simple = new ConcurrentFIFOExoCache<Serializable, Object>();
    simple.setName(config.getName());
    simple.setLabel(config.getLabel());
    simple.setMaxSize(config.getMaxSize());
    simple.setLiveTime(config.getLiveTime());
    return simple;
  }

  public Class<? extends ExoCacheConfig> getExpectedConfigType() {
        return ExoCacheConfig.class;
    }

  @Override
  public Set<String> getExpectedImplementations() {
    return Collections.singleton("simple");
  }
}
