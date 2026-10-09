package avilaos.kernel.sync;

import avilaos.model.pcb.PCB;
import avilaos.model.enumeration.ProcessType;
import avilaos.model.enumeration.ProcessState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BufferTest {

    private PCB createProducer(int computerId) {
        return new PCB("Producer", computerId, ProcessType.PRODUCER, 5, 1024, 1000, 100);
    }

    private PCB createConsumer(int computerId) {
        return new PCB("Consumer", computerId, ProcessType.CONSUMER, 5, 1024, 1000, 100);
    }

    @Test
    void testLocalPutGet() throws InterruptedException {
        Buffer buffer = new Buffer("B1", 5, 0);
        PCB producer = createProducer(0);
        PCB consumer = createConsumer(0);

        assertTrue(buffer.put(producer, "Item1"));
        assertEquals(1, buffer.getCurrentSize());
        assertEquals(4, buffer.getSemEmpty().getValue());
        assertEquals(1, buffer.getSemFull().getValue());

        Object item = buffer.get(consumer);
        assertEquals("Item1", item);
        assertEquals(0, buffer.getCurrentSize());
        assertEquals(5, buffer.getSemEmpty().getValue());
        assertEquals(0, buffer.getSemFull().getValue());
    }

    @Test
    void testBufferFull() throws InterruptedException {
        Buffer buffer = new Buffer("B1", 2, 0);
        PCB producer = createProducer(0);

        assertTrue(buffer.put(producer, "Item1"));
        assertTrue(buffer.put(producer, "Item2"));
        assertTrue(buffer.isFull());

        // Next put should block
        assertEquals(0, buffer.getSemEmpty().getValue());
    }

    @Test
    void testBufferEmpty() throws InterruptedException {
        Buffer buffer = new Buffer("B1", 2, 0);
        PCB consumer = createConsumer(0);

        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.getSemFull().getValue());

        // Get should block
    }

    @Test
    void testRemoteAccess() throws InterruptedException {
        Buffer buffer = new Buffer("B1", 5, 1); // Host is computer 1
        PCB producer = createProducer(0); // Producer on computer 0
        PCB consumer = createConsumer(0); // Consumer on computer 0

        // Put from remote computer
        boolean result = buffer.put(producer, "Item1");
        // Should complete but process was blocked for network latency
        assertTrue(result);
        assertEquals(1, buffer.getCurrentSize());

        Object item = buffer.get(consumer);
        assertEquals("Item1", item);
    }

    @Test
    void testMultipleProducersConsumers() throws InterruptedException {
        Buffer buffer = new Buffer("B1", 10, 0);
        PCB p1 = createProducer(0);
        PCB p2 = createProducer(0);
        PCB c1 = createConsumer(0);
        PCB c2 = createConsumer(0);

        buffer.put(p1, "A");
        buffer.put(p2, "B");
        assertEquals(2, buffer.getCurrentSize());

        assertEquals("A", buffer.get(c1));
        assertEquals("B", buffer.get(c2));
        assertEquals(0, buffer.getCurrentSize());
    }
}