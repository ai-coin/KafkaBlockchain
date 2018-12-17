/*
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
package com.ai_blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.log4jappender.KafkaLog4jAppender;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;


/**
 * A tamper evident log4j appender that produces log messages to Kafka
 *
 *
 */
public class TEKafkaLog4jAppender extends KafkaLog4jAppender implements Callback {

  // the message digest used for calculating the SHA-256 hash of a log event
  final private MessageDigest messageDigest;
  // the serial number of the logging event, which ensures that duplicate logging events have different hashes
  long serialNumber = 1;
  // the SHA256 hash of the previous appended log event
  private byte[] eventHash;
  // the string builder
  final StringBuilder stringBuilder = new StringBuilder();

  public TEKafkaLog4jAppender() {
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    messageDigest.reset();
  }

  /**
   * Sets the given logger to include logging to the Kafka message network.
   *
   * @param kafkaIPAddress the IP address of the Kafka broker, such as "kafka:9092" or "172.18.0.3:9092"
   * @param containerName the container name, for distinct topics
   * @param logger the logger for attaching the Kafka appender
   */
  public static void setLogger(
          final String kafkaIPAddress,
          final String containerName,
          final Logger logger) {
    //Preconditions
    assert kafkaIPAddress != null && !kafkaIPAddress.isEmpty() : "kafkaIPAddress must be a non-empty string";
    assert logger != null : "logger must not be null";

    System.out.println("tamper-evident Kafka log broker: " + kafkaIPAddress);
    final TEKafkaLog4jAppender teKafkaLog4jAppender = new TEKafkaLog4jAppender();
    teKafkaLog4jAppender.setTopic(containerName + "-log");
    teKafkaLog4jAppender.setBrokerList(kafkaIPAddress);
    teKafkaLog4jAppender.setSyncSend(false);
    teKafkaLog4jAppender.setName("KafkaLogAppender");
    teKafkaLog4jAppender.setLayout(new EnhancedPatternLayout("%d{ABSOLUTE} %t [%c{1}] %m%n"));
    teKafkaLog4jAppender.setThreshold(Level.INFO);
    teKafkaLog4jAppender.activateOptions();
    System.out.println("appender broker list: " + teKafkaLog4jAppender.getBrokerList());
    logger.addAppender(teKafkaLog4jAppender);
  }

  /**
   * Append the given logging event to the topic for this log.
   *
   * @param event the given logging event
   */
  @Override
  protected void append(final LoggingEvent event) {
    if (eventHash != null) {
      // the log events are hashed in a chain, incorporating the hash of the previous event - excepting the first one
      messageDigest.update(eventHash);
    }
    messageDigest.update(Long.toString(serialNumber).getBytes());
    final String eventString;
    if (layout == null) {
      eventString = event.getRenderedMessage().trim();
    } else {
      eventString = layout.format(event).trim();
    }
    messageDigest.update(eventString.getBytes());
    eventHash = messageDigest.digest();

    stringBuilder.setLength(0);
    stringBuilder
            .append(eventString)
            .append(" [hash ")
            .append(ByteUtils.toHex(eventHash))
            .append(']');
    final String message = stringBuilder.toString();

    final KafkaProducer producer = (KafkaProducer) getProducer();
    //LogLog.debug("[" + new Date(event.getTimeStamp()) + "]" + message);
    final String topic = getTopic();
    final boolean syncSend = this.getSyncSend();
    final ProducerRecord producerRecord = new ProducerRecord<>(topic, message.getBytes(StandardCharsets.UTF_8));
    //System.out.println("producer: " + StringUtils.toStringWithClassName(producer));
    //System.out.println("  sending producerRecord: " + producerRecord);
    @SuppressWarnings("unchecked")
    final Future<RecordMetadata> response = producer.send(
            producerRecord,
            this); // callback
    if (syncSend) {
      //System.out.println("synchronized send, get response...");
      try {
        response.get();
      } catch (InterruptedException | ExecutionException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      //System.out.println("asynchronous send for producer " + producer.toString());
    }
  }

  /**
   * A callback method the user can implement to provide asynchronous handling of request completion. This method will be called when the
   * record sent to the server has been acknowledged. Exactly one of the arguments will be non-null.
   *
   * @param metadata The metadata for the record that was sent (i.e. the partition and offset). Null if an error occurred.
   * @param exception The exception thrown during processing of this record. Null if no error occurred. Possible thrown exceptions include:
   *
   * Non-Retriable exceptions (fatal, the message will never be sent):
   *
   * InvalidTopicException OffsetMetadataTooLargeException RecordBatchTooLargeException RecordTooLargeException UnknownServerException
   *
   * Retriable exceptions (transient, may be covered by increasing #.retries):
   *
   * CorruptRecordException InvalidMetadataException NotEnoughReplicasAfterAppendException NotEnoughReplicasException
   * OffsetOutOfRangeException TimeoutException UnknownTopicOrPartitionException
   */
  @Override
  public void onCompletion(
          final RecordMetadata metadata,
          final Exception exception) {
    //Preconditions
    assert metadata == null && exception != null || metadata != null && exception == null : "one of the arguments must be non-null, the other null";

    if (metadata == null) {
      System.out.println("  onCompletion, exception: " + exception);
    } else {
      //System.out.println("  onCompletion, metadata: " + metadata);
    }
  }
}
