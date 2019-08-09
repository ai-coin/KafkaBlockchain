/*
 * ZooKeeperUtils.java
 *
 * Created on May 23, 2017, 2:03:35 PM
 *
 * Description: Provides access to the ZooKeeper distributed hash table API.
 *
 * Copyright (C) 2:03:35 PM Stephen L. Reed, all rights reserved.
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ai_blockchain.kafka_bc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * When adding SSL security, refer to https://cwiki.apache.org/confluence/display/ZOOKEEPER/ZooKeeper+SSL+User+Guide .
 *
 * @author reed
 */
public class ZooKeeperAccess {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(ZooKeeperAccess.class);
  // the ZooKeeper interface object
  private ZooKeeper zooKeeper;
  // the latch which awaits the first connnection to the ZooKeeper server
  final CountDownLatch connectedSignal_latch = new CountDownLatch(1);
  // the ZooKeeper path for the list of Kafka broker hosts
  public static final String ZK_KAFKA_BROKER_HOSTS = "/zk/kafka/broker/hosts";
  // the default port for ZooKeeper
  public static final int DEFAULT_PORT = 2181;
  // the ZooKeeper connect string
  public static final String ZOOKEEPER_CONNECT_STRING = "localhost:" + DEFAULT_PORT;

  /**
   * Constructs a new ZooKeeperAccess instance.
   */
  public ZooKeeperAccess() {
  }

  /**
   * Connect to the default ZooKeeper instances located at host "zookeeper:2181".
   *
   */
  public void connect() {
    connect(ZOOKEEPER_CONNECT_STRING);
  }

  /**
   * Connect to the ZooKeeper instances identified by entries in the given connection string. Each entry corresponds to
   * a zk server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002"
   *
   * If the optional chroot suffix is used the example would look like:
   * "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002/app/a" where the client would be rooted at "/app/a" and all paths
   * would be relative to this root - ie getting/setting/etc... "/foo/bar" would result in operations being run on
   * "/app/a/foo/bar" (from the server perspective).
   *
   * @param connectString a comma separated list of host:port pairs
   *
   */
  public void connect(final String connectString) {
    //Preconditions
    assert connectString != null && !connectString.isEmpty() : "connect string must be a non-empty string";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("connection to ZooKeeper at " + connectString);
    }
    final Watcher myWatcher = new MyWatcher();
    try {
      zooKeeper = new ZooKeeper(
              connectString,
              360000, // sessionTimeout milliseconds
              myWatcher);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    try {
      // wait for ZooKeeper to connect
      final boolean isConnected = connectedSignal_latch.await(
              30, // timeout
              TimeUnit.SECONDS); // unit
      if (!isConnected) {
        LOGGER.info("timeout while awaiting connection to the ZooKeeper instance at " + connectString);
      }
    } catch (InterruptedException ex) {
      // ignore
    }
  }

  /**
   * Create a node with the given path. The node data will be the given data, and node acl will be the given acl.
   * <p>
   * The flags argument specifies whether the created node will be ephemeral or not.
   * <p>
   * An ephemeral node will be removed by the ZooKeeper automatically when the session associated with the creation of
   * the node expires.
   * <p>
   * The flags argument can also specify to create a sequential node. The actual path name of a sequential node will be
   * the given path plus a suffix "i" where i is the current sequential number of the node. The sequence number is
   * always fixed length of 10 digits, 0 padded. Once such a node is created, the sequential number will be incremented
   * by one.
   * <p>
   * If a node with the same actual path already exists in the ZooKeeper, a KeeperException with error code
   * KeeperException.NodeExists will be thrown. Note that since a different actual path is used for each invocation of
   * creating sequential node with the same path argument, the call will never throw "file exists" KeeperException.
   * <p>
   * If the parent node does not exist in the ZooKeeper, a KeeperException with error code KeeperException.NoNode will
   * be thrown.
   * <p>
   * An ephemeral node cannot have children. If the parent node of the given path is ephemeral, a KeeperException with
   * error code KeeperException.NoChildrenForEphemerals will be thrown.
   * <p>
   * This operation, if successful, will trigger all the watches left on the node of the given path by exists and
   * getData API calls, and the watches left on the parent node by getChildren API calls.
   * <p>
   * If a node is created successfully, the ZooKeeper server will trigger the watches on the path left by exists calls,
   * and the watches on the parent of the node by getChildren calls.
   * <p>
   * The maximum allowable size of the data array is 1 MB (1,048,576 bytes). Arrays larger than this will cause a
   * KeeperExecption to be thrown.
   *
   * @param path the path for the node
   * @param data the initial data for the node
   * @param acl the acl for the node
   * @param createMode specifying whether the node to be created is ephemeral and/or sequential
   * @return the actual path of the created node
   */
  public String create(
          final String path,
          final byte data[],
          final List<ACL> acl,
          final CreateMode createMode) {
    //Preconditions
    assert path != null && !path.isEmpty() : "connect string must be a non-empty string";
    assert acl != null : "acl must not be null";
    assert createMode != null : "createMode must not be null";
    assert zooKeeper != null : "zooKeeper must not be null";

    try {
      return zooKeeper.create(
              path,
              data,
              acl,
              createMode);
    } catch (KeeperException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Returns whether the given path exists.
   *
   * @param path the given path, such as /aiblockchain/webserverHostContainer
   *
   * @return whether the given path exists
   */
  public boolean exists(final String path) {
    return null != exists(
            path,
            false); // watch
  }

  /**
   * Return the stat of the node of the given path. Return null if no such a node exists.
   * <p>
   * If the watch is true and the call is successful (no exception is thrown), a watch will be left on the node with the
   * given path. The watch will be triggered by a successful operation that creates/delete the node or sets the data on
   * the node.
   *
   * @param path the node path
   * @param watch whether need to watch this node
   * @return the stat of the node of the given path; return null if no such a node exists.
   */
  public Stat exists(
          final String path,
          final boolean watch) {
    //Preconditions
    assert path != null && !path.isEmpty() : "path must be a non-empty string";
    assert zooKeeper != null : "zooKeeper must not be null";

    try {
      return zooKeeper.exists(path, watch);
    } catch (KeeperException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Set the data for the node of the given path if such a node exists and the given version matches the version of the
   * node (if the given version is -1, it matches any node's versions). Return the stat of the node.
   * <p>
   * This operation, if successful, will trigger all the watches on the node of the given path left by getData calls.
   * <p>
   * A KeeperException with error code KeeperException.NoNode will be thrown if no node with the given path exists.
   * <p>
   * A KeeperException with error code KeeperException.BadVersion will be thrown if the given version does not match the
   * node's version.
   * <p>
   * The maximum allowable size of the data array is 1 MB (1,048,576 bytes). Arrays larger than this will cause a
   * KeeperException to be thrown.
   *
   * @param path the path of the node
   * @param data the data to set
   * @param version the expected matching version
   * @return the state of the node
   */
  public Stat setData(
          final String path,
          final byte data[],
          final int version) {
    //Preconditions
    assert path != null && !path.isEmpty() : "path must be a non-empty string";
    assert data != null : "data must not be null";
    assert zooKeeper != null : "zooKeeper must not be null";

    try {
      return zooKeeper.setData(path, data, version);
    } catch (KeeperException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Return the data and the stat of the node of the given path.
   * <p>
   * If the watch is true and the call is successful (no exception is thrown), a watch will be left on the node with the
   * given path. The watch will be triggered by a successful operation that sets data on the node, or deletes the node.
   * <p>
   * A KeeperException with error code KeeperException.NoNode will be thrown if no node with the given path exists.
   *
   * @param path the given path
   * @param watch whether need to watch this node
   * @param stat the stat of the node to update
   * @return the data of the node
   */
  public byte[] getData(
          final String path,
          final boolean watch,
          final Stat stat) {
    //Preconditions
    assert path != null && !path.isEmpty() : "path must be a non-empty string";
    assert zooKeeper != null : "zooKeeper must not be null";

    try {
      return zooKeeper.getData(path, watch, stat);
    } catch (KeeperException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Return the list of the children of the node of the given path.
   * <p>
   * If the watch is true and the call is successful (no exception is thrown), a watch will be left on the node with the
   * given path. The watch will be triggered by a successful operation that deletes the node of the given path or
   * creates/delete a child under the node.
   * <p>
   * The list of children returned is not sorted and no guarantee is provided as to its natural or lexical order.
   * <p>
   * A KeeperException with error code KeeperException.NoNode will be thrown if no node with the given path exists.
   *
   * @param path the path
   * @param watch the indicator to set a watch
   * @return an unordered array of children of the node with the given path
   */
  public List<String> getChildren(
          final String path,
          final boolean watch) {
    //Preconditions
    assert path != null && !path.isEmpty() : "path must be a non-empty string";
    assert zooKeeper != null : "zooKeeper must not be null";

    try {
      return zooKeeper.getChildren(path, watch);
    } catch (KeeperException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Delete the node with the given path. The call will succeed if such a node exists, and the given version matches the
   * node's version (if the given version is -1, it matches any node's versions).
   * <p>
   * A KeeperException with error code KeeperException.NoNode will be thrown if the nodes does not exist.
   * <p>
   * A KeeperException with error code KeeperException.BadVersion will be thrown if the given version does not match the
   * node's version.
   * <p>
   * A KeeperException with error code KeeperException.NotEmpty will be thrown if the node has children.
   * <p>
   * This operation, if successful, will trigger all the watches on the node of the given path left by exists API calls,
   * and the watches on the parent node left by getChildren API calls.
   *
   * @param path the path of the node to be deleted.
   * @param version the expected node version.
   */
  public void delete(
          final String path,
          int version) {
    //Preconditions
    assert path != null && !path.isEmpty() : "path must be a non-empty string";
    assert exists(path) : "the path must exist before it can be deleted";
    assert zooKeeper != null : "zooKeeper must not be null";

    try {
      zooKeeper.delete(path, version);
    } catch (KeeperException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Recursively delete the node with the given path.
   * <p>
   * Important: All versions, of all nodes, under the given node are deleted.
   * <p>
   * If there is an error with deleting one of the sub-nodes in the tree, this operation would abort and would be the
   * responsibility of the app to handle the same.
   *
   * @param pathRoot the path of the root node of the tree to be deleted
   */
  public void deleteRecursive(final String pathRoot) {
    //Preconditions
    assert pathRoot != null && !pathRoot.isEmpty() : "pathRoot must be a non-empty string";
    assert zooKeeper != null : "zooKeeper must not be null";

    try {
      ZKUtil.deleteRecursive(
              zooKeeper,
              pathRoot);
    } catch (KeeperException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Gets the Kafka broker seed hosts string, of the form "host1:port1, host2:port2, ...".
   *
   * @return the Kafka broker seed hosts string, or null if not found
   */
  public String getKafkaBrokerSeedHostsString() {
    //Preconditions
    assert zooKeeper != null : "zooKeeper must not be null";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getKafkaBrokerSeedHostsString ... ");
    }
    final byte[] data = getData(
            ZK_KAFKA_BROKER_HOSTS, // path
            false, // watch
            null); // stat
    if (data == null) {
      return null;
    }
    final String kafkaBrokerSeedHostsString = (String) Serialization.deserialize(data);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("  kafkaBrokerSeedHostsString " + kafkaBrokerSeedHostsString);
    }
    return kafkaBrokerSeedHostsString;
  }

  /**
   * Sets the Kafka broker seed hosts string.
   *
   * @param kafkaBrokerSeedHostsString the Kafka broker seed hosts string
   */
  public void setKafkaBrokerSeedHostsString(final String kafkaBrokerSeedHostsString) {
    //Preconditions
    assert kafkaBrokerSeedHostsString != null && !kafkaBrokerSeedHostsString.isEmpty() : "kafkaBrokerSeedHostsString must be a non-empty string";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("setKafkaBrokerSeedHostsString " + kafkaBrokerSeedHostsString + " ... ");
    }
    final byte[] data = Serialization.serialize(kafkaBrokerSeedHostsString);
    // create path node by node
    final String[] pathElements = {"texai", "kafka", "broker", "hosts"};

    String actualPath = "";
    for (final String pathElement : pathElements) {
      final String path = actualPath + "/" + pathElement.trim();
      final Stat stat = this.exists(
              path,
              false); // watch
      if (stat != null) {
        // path already exists
        continue;
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("  creating path " + path);
      }
      if (pathElement.equals("hosts")) {
        actualPath = create(
                path,
                data,
                ZooDefs.Ids.OPEN_ACL_UNSAFE, // acl
                CreateMode.PERSISTENT);
      } else {
        actualPath = create(
                path,
                new byte[0], // data
                ZooDefs.Ids.OPEN_ACL_UNSAFE, // acl
                CreateMode.PERSISTENT);
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("  actualPath    " + actualPath);
      }
    }
  }

  /**
   * Gets the string data found at the given path".
   *
   * @param path the ZooKeeper path, such as /aiblockchain/webserverHostContainer
   *
   * @return the string data
   */
  public String getDataString(final String path) {
    //Preconditions
    assert path != null && !path.isEmpty() : "path must be a non-empty string";
    assert zooKeeper != null : "zooKeeper must not be null";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getData at path " + path);
    }
    final byte[] data = getData(
            path,
            false, // watch
            null); // stat
    if (data == null) {
      return null;
    }
    final String dataString = (String) Serialization.deserialize(data);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("  dataString " + dataString);
    }
    return dataString;
  }

  /**
   * Sets the string data at the given path, creating the path if needed.
   *
   * @param path the ZooKeeper path, such as /aiblockchain/webserverHostContainer
   * @param dataString the Kafka broker seed hosts string
   */
  public void setDataString(
          final String path,
          final String dataString) {
    //Preconditions
    assert path != null && !path.isEmpty() : "path must be a non-empty string";
    assert dataString != null && !dataString.isEmpty() : "dataString must be a non-empty string";
    assert path.startsWith("/") : "path must start with /";
    assert zooKeeper != null : "zooKeeper must not be null";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("setDataString " + dataString + " at path " + path);
    }
    final byte[] data = Serialization.serialize(dataString);
    // create path node by node
    final String[] pathElements = path.substring(1).split("/");

    String actualPath = "";
    final int pathElements_len = pathElements.length;
    for (int i = 0; i < pathElements_len; i++) {
      final String pathElement = pathElements[i];
      final String path1 = actualPath + "/" + pathElement.trim();
      final Stat stat = exists(
              path1,
              false); // watch
      if (stat != null) {
        // path already exists
        actualPath = actualPath + path1;
        continue;
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("  creating path " + path1);
      }
      if (i == pathElements_len - 1) {
        // last element on the path contains the data
        actualPath = create(
                path1,
                data,
                ZooDefs.Ids.OPEN_ACL_UNSAFE, // acl
                CreateMode.PERSISTENT);
        LOGGER.info("setDataString " + dataString + " at path " + actualPath);
      } else {
        actualPath = create(
                path1,
                new byte[0], // data
                ZooDefs.Ids.OPEN_ACL_UNSAFE, // acl
                CreateMode.PERSISTENT);
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("  actualPath    " + actualPath);
      }
    }
  }

  /**
   * Returns a string representation of this object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    if (zooKeeper == null) {
      return "";
    } else {
      return zooKeeper.toString();
    }
  }

  /**
   * Disconnect from the ZooKeeper cluster.
   *
   */
  public void close() {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("closing the ZooKeeper connection ...");
    }
    if (zooKeeper != null) {
      try {
        zooKeeper.close();
      } catch (InterruptedException ex) {
        // ignore
      }
    }
  }

  final class MyWatcher implements Watcher {

    @Override
    public void process(final WatchedEvent watchedEvent) {
      //Preconditions
      assert watchedEvent != null : "watchedEvent must not be null";

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("watchedEvent: " + watchedEvent);
      }

      if (watchedEvent.getState() == KeeperState.SyncConnected) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("ZooKeeper session is connected");
        }
        connectedSignal_latch.countDown();
      } else if (watchedEvent.getState() == KeeperState.Disconnected) {
        LOGGER.info("ZooKeeper session is disconnected, reconnecting ...");
        connect();
      }
    }
  }

}
