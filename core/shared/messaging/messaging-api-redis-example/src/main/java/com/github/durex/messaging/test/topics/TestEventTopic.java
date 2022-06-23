package com.github.durex.messaging.test.topics;

import com.github.durex.messaging.api.annotation.Topic;
import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.api.model.EventTopic;
import com.github.durex.messaging.test.models.TestEvent;

@Topic(value = TopicNameConstants.TEST_TOPIC, codec = CodecEnum.JSON)
public interface TestEventTopic extends EventTopic<TestEvent> {}
