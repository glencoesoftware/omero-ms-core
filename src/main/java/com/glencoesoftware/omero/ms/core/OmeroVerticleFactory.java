/*
 * Copyright (C) 2019 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.glencoesoftware.omero.ms.core;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

/**
 * Spring backed OMERO verticle factory.  Implementation cribbed from the
 * <code>SpringVerticleFactory</code> of the Vert.x Spring worker example.
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class OmeroVerticleFactory
        implements VerticleFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public boolean blockingCreate() {
        // Usually verticle instantiation is fast but since our verticles are
        // Spring Beans, they might depend on other beans/resources which are
        // slow to build/lookup.
        return true;
    }

    /* (non-Javadoc)
     * @see io.vertx.core.spi.VerticleFactory#prefix()
     */
    @Override
    public String prefix() {
        return "omero";
    }

    /* (non-Javadoc)
     * @see io.vertx.core.spi.VerticleFactory#createVerticle(java.lang.String, java.lang.ClassLoader)
     */
    @Override
    public Verticle createVerticle(
            String verticleName, ClassLoader classLoader)
                    throws Exception {
        return (Verticle) applicationContext.getBean(
                VerticleFactory.removePrefix(verticleName));
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

  }
