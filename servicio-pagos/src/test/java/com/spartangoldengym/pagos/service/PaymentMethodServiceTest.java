package com.spartangoldengym.pagos.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.pagos.dto.PaymentMethodRequest;
import com.spartangoldengym.pagos.dto.PaymentMethodResponse;
import com.spartangoldengym.pagos.entity.PaymentMethod;
import com.spartangoldengym.pagos.gateway.PaymentGateway;
import com.spartangoldengym.pagos.gateway.PaymentGatewayFactory;
import com.spartangoldengym.pagos.repository.PaymentMethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private PaymentGatewayFactory paymentGatewayFactory;
    @Mock private AuditService auditService;
    @Mock private PaymentGateway paymentGateway;

    private PaymentMethodService paymentMethodService;

    @BeforeEach
    void setUp() {
        paymentMethodService = new PaymentMethodService(
                paymentMethodRepository, paymentGatewayFactory, auditService);
    }

    @Test
    void addPaymentMethod_createsMethodWithExternalId() {
        UUID userId = UUID.randomUUID();
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.setUserId(userId);
        request.setPaymentProvider("stripe");
        request.setPaymentToken("tok_visa");
        request.setCardLastFour("4242");
        request.setCardBrand("visa");
        request.setDefault(true);

        when(paymentGatewayFactory.getGateway("stripe")).thenReturn(paymentGateway);
        when(paymentGateway.createPaymentMethod("tok_visa")).thenReturn("stripe_pm_abc");
        when(paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.empty());
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(inv -> {
            PaymentMethod m = inv.getArgument(0);
            if (m.getId() == null) m.setId(UUID.randomUUID());
            return m;
        });

        PaymentMethodResponse response = paymentMethodService.addPaymentMethod(request);

        assertNotNull(response.getId());
        assertEquals("4242", response.getCardLastFour());
        assertEquals("visa", response.getCardBrand());
        assertTrue(response.isDefault());
    }

    @Test
    void getUserPaymentMethods_returnsAllMethods() {
        UUID userId = UUID.randomUUID();
        PaymentMethod m1 = new PaymentMethod();
        m1.setId(UUID.randomUUID());
        m1.setUserId(userId);
        m1.setPaymentProvider("stripe");
        m1.setCardLastFour("4242");
        m1.setDefault(true);

        PaymentMethod m2 = new PaymentMethod();
        m2.setId(UUID.randomUUID());
        m2.setUserId(userId);
        m2.setPaymentProvider("adyen");
        m2.setCardLastFour("1234");
        m2.setDefault(false);

        when(paymentMethodRepository.findByUserId(userId)).thenReturn(Arrays.asList(m1, m2));

        List<PaymentMethodResponse> methods = paymentMethodService.getUserPaymentMethods(userId);

        assertEquals(2, methods.size());
    }

    @Test
    void deletePaymentMethod_removesFromDbAndProvider() {
        UUID methodId = UUID.randomUUID();
        PaymentMethod method = new PaymentMethod();
        method.setId(methodId);
        method.setUserId(UUID.randomUUID());
        method.setPaymentProvider("stripe");
        method.setExternalMethodId("stripe_pm_xyz");

        when(paymentMethodRepository.findById(methodId)).thenReturn(Optional.of(method));
        when(paymentGatewayFactory.getGateway("stripe")).thenReturn(paymentGateway);

        paymentMethodService.deletePaymentMethod(methodId);

        verify(paymentGateway).deletePaymentMethod("stripe_pm_xyz");
        verify(paymentMethodRepository).delete(method);
    }

    @Test
    void deletePaymentMethod_notFound_throwsException() {
        UUID methodId = UUID.randomUUID();
        when(paymentMethodRepository.findById(methodId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentMethodService.deletePaymentMethod(methodId));
    }
}
