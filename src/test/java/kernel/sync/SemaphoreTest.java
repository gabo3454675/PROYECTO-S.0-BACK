package avilaos.kernel.sync;

import avilaos.model.pcb.PCB;
import avilaos.model.enumeration.ProcessType;
import avilaos.model.enumeration.ProcessState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SemaphoreTest {

    private PCB createPCB() {
        PCB pcb = new PCB("Test", 0, ProcessType.CPU_BOUND, 5, 1024, 1000, 100);
        pcb.setState(ProcessState.RUNNING); // Simulate running process
        return pcb;
    }

    @Test
    void testSemaphoreWaitSignal() {
        Semaphore sem = new Semaphore(1);
        PCB pcb = createPCB();

        sem.wait(pcb);
        assertEquals(0, sem.getValue());
        assertEquals(ProcessState.RUNNING, pcb.getState()); // Not blocked

        sem.signal();
        assertEquals(1, sem.getValue());
    }

    @Test
    void testSemaphoreBlocking() {
        Semaphore sem = new Semaphore(0);
        PCB pcb = createPCB();

        sem.wait(pcb);
        assertEquals(-1, sem.getValue());
        assertEquals(ProcessState.BLOCKED, pcb.getState());
        assertEquals(1, sem.getWaitingCount());

        sem.signal();
        assertEquals(0, sem.getValue());
        assertEquals(ProcessState.READY, pcb.getState());
        assertEquals(0, sem.getWaitingCount());
    }

    @Test
    void testMultipleWaiters() {
        Semaphore sem = new Semaphore(0);
        PCB p1 = createPCB();
        PCB p2 = createPCB();
        PCB p3 = createPCB();

        sem.wait(p1);
        sem.wait(p2);
        sem.wait(p3);

        assertEquals(-3, sem.getValue());
        assertEquals(3, sem.getWaitingCount());

        sem.signal();
        assertEquals(-2, sem.getValue());
        assertEquals(2, sem.getWaitingCount());

        sem.signal();
        sem.signal();
        assertEquals(0, sem.getValue());
        assertEquals(0, sem.getWaitingCount());
    }
}