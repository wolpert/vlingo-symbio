// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.symbio.store.state.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.state.Entity1;
import io.vlingo.symbio.store.state.MockResultInterest;
import io.vlingo.symbio.store.state.MockTextDispatcher;
import io.vlingo.symbio.store.state.StateStore.Result;
import io.vlingo.symbio.store.state.StateTypeStateStoreMap;
import io.vlingo.symbio.store.state.TextStateStore;

public class InMemoryTextStateStoreTest {
  private final static String StoreName = Entity1.class.getSimpleName();

  private MockTextDispatcher dispatcher;
  private TextStateStore store;
  private World world;

  @Test
  public void testThatStateStoreWritesText() {
    final Entity1 entity = new Entity1("123", 5);
    final String serializedState = JsonSerialization.serialized(entity);
    final MockResultInterest interest = new MockResultInterest(1);

    store.write(new TextState(entity.id, Entity1.class, 1, serializedState, 1), interest);

    interest.until.completes();

    assertEquals(0, interest.readTextResultedIn.get());
    assertEquals(1, interest.writeTextResultedIn.get());
    assertEquals(Result.Success, interest.textWriteResult.get());
    assertEquals(serializedState, interest.textState.get().data);
  }

  @Test
  public void testThatStateStoreWritesAndReadsText() {
    final Entity1 entity = new Entity1("123", 5);
    final String serializedState = JsonSerialization.serialized(entity);
    final MockResultInterest interest = new MockResultInterest(2);

    store.write(new TextState(entity.id, Entity1.class, 1, serializedState, 1), interest);
    store.read(entity.id, Entity1.class, interest);

    interest.until.completes();

    assertEquals(1, interest.readTextResultedIn.get());
    assertEquals(1, interest.writeTextResultedIn.get());
    assertEquals(Result.Success, interest.textReadResult.get());
    assertEquals(serializedState, interest.textState.get().data);
    assertEquals(1, interest.textState.get().typeVersion);
    assertFalse(interest.textState.get().hasMetadata());

    final Entity1 readEntity = JsonSerialization.deserialized(interest.textState.get().data, Entity1.class);

    assertEquals("123", readEntity.id);
    assertEquals(5, readEntity.value);
  }

  @Test
  public void testThatStateStoreWritesAndReadsMetadataValue() {
    final Entity1 entity = new Entity1("123", 5);
    final String serializedState = JsonSerialization.serialized(entity);
    final MockResultInterest interest = new MockResultInterest(2);

    store.write(new TextState(entity.id, Entity1.class, 1, serializedState, 1, Metadata.withValue("value")), interest);
    store.read(entity.id, Entity1.class, interest);

    interest.until.completes();

    assertEquals(1, interest.readTextResultedIn.get());
    assertEquals(1, interest.writeTextResultedIn.get());
    assertEquals(Result.Success, interest.textReadResult.get());
    assertEquals(serializedState, interest.textState.get().data);
    assertEquals(1, interest.textState.get().typeVersion);
    assertTrue(interest.textState.get().hasMetadata());
    assertTrue(interest.textState.get().metadata.hasValue());
    assertEquals("value", interest.textState.get().metadata.value);
    assertFalse(interest.textState.get().metadata.hasOperation());

    final Entity1 readEntity = JsonSerialization.deserialized(interest.textState.get().data, Entity1.class);

    assertEquals("123", readEntity.id);
    assertEquals(5, readEntity.value);
  }

  @Test
  public void testThatStateStoreWritesAndReadsMetadataOperation() {
    final Entity1 entity = new Entity1("123", 5);
    final String serializedState = JsonSerialization.serialized(entity);
    final MockResultInterest interest = new MockResultInterest(2);

    store.write(new TextState(entity.id, Entity1.class, 1, serializedState, 1, Metadata.withOperation("op")), interest);
    store.read(entity.id, Entity1.class, interest);

    interest.until.completes();

    assertEquals(1, interest.readTextResultedIn.get());
    assertEquals(1, interest.writeTextResultedIn.get());
    assertEquals(Result.Success, interest.textReadResult.get());
    assertEquals(serializedState, interest.textState.get().data);
    assertEquals(1, interest.textState.get().typeVersion);
    assertTrue(interest.textState.get().hasMetadata());
    assertFalse(interest.textState.get().metadata.hasValue());
    assertTrue(interest.textState.get().metadata.hasOperation());
    assertEquals("op", interest.textState.get().metadata.operation);

    final Entity1 readEntity = JsonSerialization.deserialized(interest.textState.get().data, Entity1.class);

    assertEquals("123", readEntity.id);
    assertEquals(5, readEntity.value);
  }

  @Test
  public void testThatStateStoreWritesAndReadsMetadata() {
    final Entity1 entity = new Entity1("123", 5);
    final String serializedState = JsonSerialization.serialized(entity);
    final MockResultInterest interest = new MockResultInterest(2);

    store.write(new TextState(entity.id, Entity1.class, 1, serializedState, 1, Metadata.withOperation("op")), interest);
    store.read(entity.id, Entity1.class, interest);

    interest.until.completes();

    assertEquals(1, interest.readTextResultedIn.get());
    assertEquals(1, interest.writeTextResultedIn.get());
    assertEquals(Result.Success, interest.textReadResult.get());
    assertEquals(serializedState, interest.textState.get().data);
    assertEquals(1, interest.textState.get().typeVersion);
    assertTrue(interest.textState.get().hasMetadata());
    assertFalse(interest.textState.get().metadata.hasValue());
    assertTrue(interest.textState.get().metadata.hasOperation());
    assertEquals("op", interest.textState.get().metadata.operation);

    final Entity1 readEntity = JsonSerialization.deserialized(interest.textState.get().data, Entity1.class);

    assertEquals("123", readEntity.id);
    assertEquals(5, readEntity.value);
  }

  @Test
  public void testThatStateStoreDispatches() {
    final MockResultInterest interest = new MockResultInterest(3);

    dispatcher.until = TestUntil.happenings(3);

    final Entity1 entity1 = new Entity1("123", 1);
    store.write(new TextState(entity1.id, Entity1.class, 1, JsonSerialization.serialized(entity1), 1), interest);
    final Entity1 entity2 = new Entity1("234", 2);
    store.write(new TextState(entity2.id, Entity1.class, 1, JsonSerialization.serialized(entity2), 1), interest);
    final Entity1 entity3 = new Entity1("345", 3);
    store.write(new TextState(entity3.id, Entity1.class, 1, JsonSerialization.serialized(entity3), 1), interest);

    dispatcher.until.completes();

    assertEquals(3, dispatcher.dispatched.size());
    assertEquals("123", dispatcher.dispatched.get(dispatchId("123")).id);
    assertEquals("234", dispatcher.dispatched.get(dispatchId("234")).id);
    assertEquals("345", dispatcher.dispatched.get(dispatchId("345")).id);

    dispatcher.until = TestUntil.happenings(2);

    dispatcher.processDispatch.set(false);
    final Entity1 entity4 = new Entity1("456", 4);
    store.write(new TextState(entity4.id, Entity1.class, 1, JsonSerialization.serialized(entity4), 1), interest);
    final Entity1 entity5 = new Entity1("567", 5);
    store.write(new TextState(entity5.id, Entity1.class, 1, JsonSerialization.serialized(entity5), 1), interest);
    dispatcher.processDispatch.set(true);
    dispatcher.control.dispatchUnconfirmed();

    dispatcher.until.completes();

    assertEquals(5, dispatcher.dispatched.size());
    assertEquals("456", dispatcher.dispatched.get(dispatchId("456")).id);
    assertEquals("567", dispatcher.dispatched.get(dispatchId("567")).id);
  }

  @Before
  public void setUp() {
    world = World.start("test-store", true);

    dispatcher = new MockTextDispatcher();

    store = world.actorFor(Definition.has(InMemoryTextStateStoreActor.class, Definition.parameters(dispatcher)), TextStateStore.class);

    StateTypeStateStoreMap.stateTypeToStoreName(Entity1.class, StoreName);
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  private String dispatchId(final String entityId) {
    return StoreName + ":" + entityId;
  }
}