/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.snowdrop.stream.binder.artemis;

import me.snowdrop.stream.binder.artemis.application.FirstReceiver;
import me.snowdrop.stream.binder.artemis.application.SecondReceiver;
import me.snowdrop.stream.binder.artemis.application.Sender;
import me.snowdrop.stream.binder.artemis.application.StreamApplication;
import me.snowdrop.stream.binder.artemis.utils.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static me.snowdrop.stream.binder.artemis.utils.AwaitUtils.awaitForHandledMessages;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = StreamApplication.class,
        properties = {
                "spring.cloud.stream.bindings.input.group=testGroup",
                "spring.cloud.stream.bindings.alternativeInput.group=testGroup"
        }
)
public class SingleGroupEndToEndIT {

    private static final String[] MESSAGES = {
            "Test message 1", "Test message 2", "Test message 3", "Test message 4"
    };

    @Autowired
    private Sender sender;

    @Autowired
    private FirstReceiver firstReceiver;

    @Autowired
    private SecondReceiver secondReceiver;

    @Before
    public void before() {
        firstReceiver.clear();
        secondReceiver.clear();
    }

    @Test
    public void shouldSplitMessagesBetweenReceivers() {
        for (String message : MESSAGES) {
            sender.send(message);
        }

        awaitForHandledMessages(firstReceiver, 2);
        awaitForHandledMessages(secondReceiver, 2);

        List<Message> messages = firstReceiver.getHandledMessages();
        messages.addAll(secondReceiver.getHandledMessages());

        Assertions.assertPayload(messages, MESSAGES);
    }

}
