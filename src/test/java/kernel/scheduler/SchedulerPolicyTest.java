package avilaos.kernel.scheduler;

import avilaos.model.pcb.PCB;
import avilaos.model.enumeration.ProcessType;
import avilaos.util.datastructures.LinkedQueue;
import avilaos.util.datastructures.Queue;
import avilaos.util.IDGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SchedulerPolicyTest {

    @BeforeEach
    void resetIdGenerator() {
        IDGenerator.reset();
    }

    private PCB createPCB(String name, int priority, long deadline) {
        return new PCB(name, 0, ProcessType.CPU_BOUND, priority, 1024, deadline, 100);
    }

    @Test
    void testFCFS() {
        FCFSFPolicy policy = new FCFSFPolicy();
        Queue<PCB> queue = new LinkedQueue<>();

        PCB p1 = createPCB("P1", 5, 1000);
        PCB p2 = createPCB("P2", 3, 500);
        PCB p3 = createPCB("P3", 1, 2000);

        queue.enqueue(p1);
        queue.enqueue(p2);
        queue.enqueue(p3);

        assertEquals(p1, policy.selectNext(queue));
        queue.remove(p1);
        assertEquals(p2, policy.selectNext(queue));
        queue.remove(p2);
        assertEquals(p3, policy.selectNext(queue));
    }

    @Test
    void testRoundRobin() {
        RoundRobinPolicy policy = new RoundRobinPolicy(3);
        Queue<PCB> queue = new LinkedQueue<>();

        PCB p1 = createPCB("P1", 5, 1000);
        PCB p2 = createPCB("P2", 3, 500);

        queue.enqueue(p1);
        queue.enqueue(p2);

        assertEquals(p1, policy.selectNext(queue));
        policy.incrementQuantum();
        policy.incrementQuantum();
        policy.incrementQuantum();
        assertTrue(policy.isQuantumExpired());
        policy.onQuantumExpired();
        assertFalse(policy.isQuantumExpired());
    }

    @Test
    void testEDF() {
        EDFPolicy policy = new EDFPolicy();
        Queue<PCB> queue = new LinkedQueue<>();

        PCB p1 = createPCB("P1", 5, 1000);
        PCB p2 = createPCB("P2", 3, 500);
        PCB p3 = createPCB("P3", 1, 2000);

        queue.enqueue(p1);
        queue.enqueue(p2);
        queue.enqueue(p3);

        // EDF: earliest deadline first (p2=500, p1=1000, p3=2000)
        PCB selected = policy.selectNext(queue);
        assertEquals(p2, selected);
        queue.remove(selected);

        selected = policy.selectNext(queue);
        assertEquals(p1, selected);
        queue.remove(selected);

        selected = policy.selectNext(queue);
        assertEquals(p3, selected);
    }

    @Test
    void testPriorityPreemptive() {
        PriorityPreemptivePolicy policy = new PriorityPreemptivePolicy();
        Queue<PCB> queue = new LinkedQueue<>();

        PCB p1 = createPCB("P1", 5, 1000); // lowest priority (highest number)
        PCB p2 = createPCB("P2", 3, 500);  // medium priority
        PCB p3 = createPCB("P3", 1, 2000); // highest priority (lowest number)

        queue.enqueue(p1);
        queue.enqueue(p2);
        queue.enqueue(p3);

        // Priority: lower number = higher priority
        PCB selected = policy.selectNext(queue);
        assertEquals(p3, selected); // priority 1
        queue.remove(selected);

        selected = policy.selectNext(queue);
        assertEquals(p2, selected); // priority 3
        queue.remove(selected);

        selected = policy.selectNext(queue);
        assertEquals(p1, selected); // priority 5

        // Test preemption: incoming has higher priority (lower number)
        PCB running = createPCB("Running", 5, 1000);
        PCB incoming = createPCB("Incoming", 2, 500);
        assertTrue(policy.shouldPreempt(running, incoming));
    }
}