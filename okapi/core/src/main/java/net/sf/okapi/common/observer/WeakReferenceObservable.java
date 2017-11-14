/*
 Copyright (C) 2007 Richard Gomes

 This source code is release under the BSD License.
 
 This file is part of JQuantLib, a free-software/open-source library
 for financial quantitative analysts and developers - http://jquantlib.org/

 JQuantLib is free software: you can redistribute it and/or modify it
 under the terms of the JQuantLib license.  You should have received a
 copy of the license along with this program; if not, please email
 <jquant-devel@lists.sourceforge.net>. The license is also available online at
 <http://www.jquantlib.org/index.php/LICENSE.TXT>.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE.  See the license for more details.
 
 JQuantLib is based on QuantLib. http://quantlib.org/
 When applicable, the original copyright notice follows this notice.
 */
/*===========================================================================
  Additional changes:
  Copyright (C) 2008-2009 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.common.observer;

import java.lang.ref.WeakReference;

/**
 * Implementation of {@link IObservable} that holds references to Observers as
 * WeakReferences.
 * 
 * This implementation notifies the observers in a synchronous
 * fashion. Note that this can cause trouble if you notify the observers while
 * in a transactional context because the notification is then done also in the
 * transaction.
 * 
 * <p>
 * This class is based on the work done by Martin Fischer. See references below.
 * 
 * @see <a
 *      href="http://www.jroller.com/martin_fischer/entry/a_generic_java_observer_pattern">
 *      Martin Fischer: Observer and Observable interfaces</a>
 * @see <a href="http://jdj.sys-con.com/read/35878.htm">Improved Observer/Observable</a>
 * 
 * @see IObservable
 * @see IObserver
 * @see BaseObservable
 * 
 * @author Martin Fischer (original author)
 * @author Richard Gomes
 * @author Srinivas Hasti
 */
public class WeakReferenceObservable extends BaseObservable {

	/**
	 * Creates a new WeakReferenceObservable object with a given {@link IObservable} object.
	 * @param observable the object to observe.
	 */
    public WeakReferenceObservable(IObservable observable) {
        super(observable);
    }

    @Override
    public void addObserver(IObserver observer) {
        super.addObserver(new WeakReferenceObserver(observer));
    }

    @Override
    public void deleteObserver(IObserver observer) {
        // Also deletes weak references whose referents got gc'ed
        for (IObserver weakObserver : getObservers()) {
            WeakReferenceObserver weakReference = (WeakReferenceObserver) weakObserver;
            IObserver o = weakReference.get();
            if (o == null || o.equals(observer)) {
                deleteWeakReference(weakReference);
            }
        }
    }
    
    private void deleteWeakReference(WeakReferenceObserver observer){
        super.deleteObserver(observer);
    }
    
    //
    // Inner class
    //
    private class WeakReferenceObserver extends WeakReference<IObserver> implements IObserver {
    	
        public WeakReferenceObserver(IObserver referent) {
            super(referent);
        }

        public void update(IObservable o, Object arg) {
            IObserver referent = get();
            if (referent != null)
                referent.update(o, arg);  
            else //delete the weak reference from the list if underlying gc'ed
                deleteWeakReference(this);
        }
    }

}
