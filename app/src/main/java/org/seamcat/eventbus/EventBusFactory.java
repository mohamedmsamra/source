/*
Copyright (c) 2010, Adam Taft
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

* Neither the name of the project owner nor the names of its contributors
may be used to endorse or promote products derived from this software
without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.seamcat.eventbus;

/**
 * A factory for creating instances of {@link EventBus}.  The most common use
 * case will be to call {@link #getEventBus()}, which returns a {@link BasicEventBus}
 * singleton instance.  Other EventBus implementations can, however, be created and
 * used easily with this factory.
 *
 * @author Adam Taft
 */
public class EventBusFactory {

	/**
	 * Creates and returns an {@link EventBus} instances as specified by the
	 * provided event bus class.  Uses {@link Class#newInstance()} to create the
	 * new instance of the event bus.
	 * 
	 * @param eventBusClass The class used to create the event bus instance.
	 * @return  The specified {@link EventBus}
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static EventBus newEventBus(Class<? extends EventBus> eventBusClass) throws InstantiationException, IllegalAccessException {
		return eventBusClass.newInstance();
	}
	
	/**
	 * Creates and returns an {@link EventBus} instance based on the specified
	 * class name.  This will create the bus using the {@link Class#forName(String)}
	 * method.
	 * 
	 * @param eventBusClassName The fully qualified class name.
	 * @return An instance of the specified EventBus class.
	 * 
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static EventBus newEventBus(String eventBusClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return newEventBus((Class<? extends EventBus>) Class.forName(eventBusClassName));
	}
	
	/**
	 * Creates and returns a <b>new</b> {@link BasicEventBus}.  This would
	 * not usually be very useful unless separate {@link EventBus} instances
	 * were needed (for a more complex application or something).
	 * 
	 * @return A new instance of {@link BasicEventBus}
	 */
	public static EventBus newEventBus() {
		try {
			return newEventBus(BasicEventBus.class);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static EventBus instance;
	
	
	/**
	 * Creates (if necessary) and returns a singleton instance of a
	 * {@link BasicEventBus}.  This will likely be the most common
	 * use case for most applications.
	 * 
	 * @return A singleton instance of {@link BasicEventBus}
	 */
	public static synchronized EventBus getEventBus() {
		if (instance == null) {
			instance = newEventBus();
		}
		return instance;
	}

}
