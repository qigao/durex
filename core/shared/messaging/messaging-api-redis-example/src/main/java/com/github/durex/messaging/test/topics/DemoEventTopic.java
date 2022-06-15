package com.github.durex.messaging.test.topics;

import com.github.durex.messaging.api.annotation.Topic;
import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.api.model.EventTopic;
import com.github.durex.messaging.test.models.DemoEvent;

@Topic(value = TopicNameConstants.DEMO_TOPIC, codec = CodecEnum.MSGPACK)
public interface DemoEventTopic extends EventTopic<DemoEvent> {}
