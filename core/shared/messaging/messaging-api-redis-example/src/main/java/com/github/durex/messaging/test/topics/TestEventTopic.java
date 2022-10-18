package com.github.durex.messaging.test.topics;

import static com.github.durex.messaging.api.enums.CodecEnum.JSON;

import com.github.durex.messaging.api.annotation.Topic;
import com.github.durex.messaging.api.model.EventTopic;
import com.github.durex.messaging.test.models.TestEvent;

@Topic(
    value = TopicNameConstants.TEST_TOPIC,
    codec = JSON,
    group = "test_group",
    subscriber = "consumer")
public interface TestEventTopic extends EventTopic<TestEvent> {}
