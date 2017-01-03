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
 * An EventBus is a simple pattern to promote loose coupling between various
 * components.  An event bus can be used in place of a traditional listener
 * or observer pattern, and is useful when ties between multiple components
 * become too complicated to track.
 * <p>
 * A traditional use case for an event bus is a Swing based application, where
 * multiple actions and listeners are configured to capture the various events
 * of the application, such as mouse clicks, window events, data loading events,
 * etc.  Swing promotes a one-to-one mapping between listener/listenee components,
 * and as such, it can become difficult to configure the various listeners
 * without tightly coupling all the various componenets together.
 * <p>
 * With an event bus, events can be published to the bus and any class can be
 * configured to listen for such events.  Thus, each individual component only
 * needs to be tightly coupled with the event bus, and it can then receive
 * notifications about events that it cares to know about.
 * <p>
 * The event bus pattern has a simple interface, with a subscribe/publish type
 * model.  Any object can be subscribed to the event bus, but to received messages
 * from the bus, the object must have its methods annotated with the {@link EventHandler}
 * annotation.  This annotation marks the methods of the subscriber class which
 * should be used to receive event bus events.
 * <p>
 * A published event has the potential to be vetoed and thus not propagated to
 * other non-vetoing subscribers.  This is accomplished by setting the
 * {@link EventHandler#canVeto()} property to true and throwing a {@link VetoException}
 * when the method is called from the EventBus.  The event bus will note the
 * veto and not relay the message to the subscribers, but will instead send a
 * {@link VetoEvent} out on the bus indicating that the published event has been
 * vetoed.
 *
 * @author Adam Taft
 */
public interface EventBus {

	/**
	 * Subscribes the specified subscriber to the event bus.  A subscribed object
	 * will be notified of any published events on the methods annotated with the
	 * {@link EventHandler} annotation.
	 * <p>
	 * Each event handler method should take a single parameter indicating the
	 * type of event it wishes to receive.  When events are published on the
	 * bus, only subscribers who have an EventHandler method with a matching
	 * parameter of the same type as the published event will receive the
	 * event notification from the bus.
	 * 
	 * @param subscriber The object to subscribe to the event bus.
	 */
	void subscribe(Object subscriber);
	
	
	/**
	 * Removes the specified object from the event bus subscription list.  Once
	 * removed, the specified object will no longer receive events posted to the
	 * event bus.
	 * 
	 * @param subscriber The object previous subscribed to the event bus.
	 */
	void unsubscribe(Object subscriber);
	
	
	/**
	 * Sends a message on the bus which will be propagated to the appropriate
	 * subscribers of the event type.  Only subscribers which have elected to
	 * subscribe to the same event type as the supplied event will be notified
	 * of the event.
	 * <p>
	 * Events can be vetoed, indicating that the event should not propagate to
	 * the subscribers that don't have a veto.  The subscriber can veto by
	 * setting the {@link EventHandler#canVeto()} return to true and by throwing
	 * a {@link VetoException}.
	 * <p>
	 * There is no specification given as to how the messages will be delivered,
	 * in terms of synchronous or asynchronous.  The only requirement is that
	 * all the event handlers that can issue vetos be called before non-vetoing
	 * handlers.  Most implementations will likely deliver messages asynchronously.
	 * 
	 * @param event The event to send out to the subscribers of the same type.
	 */
	void publish(Object event);
	
	
	/**
	 * Indicates whether the bus has pending events to publish.  Since message/event
	 * delivery can be asynchronous (on other threads), the method can be used to
	 * start or stop certain actions based on all the events having been published.
	 * I.e. perhaps before an application closes, etc. 
	 * 
	 * @return True if events are still being delivered.
	 */
	boolean hasPendingEvents();

}
