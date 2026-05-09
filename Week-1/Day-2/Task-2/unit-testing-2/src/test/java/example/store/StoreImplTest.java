package example.store;

import example.account.AccountManager;
import example.account.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StoreImplTest {

    private AccountManager accountManager;
    private Store store;
    private Product product;
    private Customer customer;

    @BeforeEach
    void setUp() {
        accountManager = mock(AccountManager.class);
        store = new StoreImpl(accountManager);
        product = new Product();
        customer = new Customer();
    }

    // Happy scenario
    @Test
    void givenProductInStock_whenBuy_thenQuantityDecremented() {
        product.setQuantity(5);
        product.setPrice(100);
        when(accountManager.withdraw(customer, 100)).thenReturn("success");

        store.buy(product, customer);

        assertEquals(4, product.getQuantity());
    }

    // Out of stock
    @Test
    void givenProductOutOfStock_whenBuy_thenThrowsRuntimeException() {
        product.setQuantity(0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> store.buy(product, customer));

        assertEquals("Product out of stock", ex.getMessage());
    }

    @Test
    void givenProductOutOfStock_whenBuy_thenAccountManagerNeverCalled() {
        product.setQuantity(0);

        assertThrows(RuntimeException.class, () -> store.buy(product, customer));

        // No interaction with the payment system should occur
        verifyNoInteractions(accountManager);
    }

    // Payment failure
    @Test
    void givenPaymentFailure_whenBuy_thenThrowsRuntimeExceptionWithMessage() {
        product.setQuantity(3);
        product.setPrice(200);
        when(accountManager.withdraw(customer, 200)).thenReturn("insufficient account balance");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> store.buy(product, customer));

        assertTrue(ex.getMessage().startsWith("Payment failure:"));
        assertTrue(ex.getMessage().contains("insufficient account balance"));
    }

    @Test
    void givenPaymentFailure_whenBuy_thenQuantityNotDecremented() {
        product.setQuantity(3);
        product.setPrice(200);
        when(accountManager.withdraw(customer, 200)).thenReturn("insufficient account balance");

        assertThrows(RuntimeException.class, () -> store.buy(product, customer));

        // Stock must not be decremented on failed payment
        assertEquals(3, product.getQuantity());
    }

    @Test
    void givenMaxCreditExceeded_whenBuy_thenThrowsRuntimeException() {
        product.setQuantity(1);
        product.setPrice(500);
        when(accountManager.withdraw(customer, 500)).thenReturn("maximum credit exceeded");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> store.buy(product, customer));

        assertTrue(ex.getMessage().contains("Payment failure"));
    }
}
