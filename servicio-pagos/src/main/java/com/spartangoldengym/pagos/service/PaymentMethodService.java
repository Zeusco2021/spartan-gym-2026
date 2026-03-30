package com.spartangoldengym.pagos.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.pagos.dto.PaymentMethodRequest;
import com.spartangoldengym.pagos.dto.PaymentMethodResponse;
import com.spartangoldengym.pagos.entity.PaymentMethod;
import com.spartangoldengym.pagos.gateway.PaymentGateway;
import com.spartangoldengym.pagos.gateway.PaymentGatewayFactory;
import com.spartangoldengym.pagos.repository.PaymentMethodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentMethodService {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodService.class);

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final AuditService auditService;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
                                PaymentGatewayFactory paymentGatewayFactory,
                                AuditService auditService) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getUserPaymentMethods(UUID userId) {
        return paymentMethodRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentMethodResponse addPaymentMethod(PaymentMethodRequest request) {
        PaymentGateway gateway = paymentGatewayFactory.getGateway(request.getPaymentProvider());
        String externalMethodId = gateway.createPaymentMethod(request.getPaymentToken());

        if (request.isDefault()) {
            paymentMethodRepository.findByUserIdAndIsDefaultTrue(request.getUserId())
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                        paymentMethodRepository.save(existing);
                    });
        }

        PaymentMethod method = new PaymentMethod();
        method.setUserId(request.getUserId());
        method.setPaymentProvider(request.getPaymentProvider());
        method.setExternalMethodId(externalMethodId);
        method.setCardLastFour(request.getCardLastFour());
        method.setCardBrand(request.getCardBrand());
        method.setDefault(request.isDefault());
        method = paymentMethodRepository.save(method);

        auditService.log(request.getUserId(), "PAYMENT_METHOD_ADDED",
                "payment_method", method.getId().toString(),
                "{\"provider\":\"" + request.getPaymentProvider() + "\",\"lastFour\":\"" + request.getCardLastFour() + "\"}");

        log.info("Payment method added: userId={} methodId={}", request.getUserId(), method.getId());
        return toResponse(method);
    }

    @Transactional
    public void deletePaymentMethod(UUID methodId) {
        PaymentMethod method = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", methodId.toString()));

        PaymentGateway gateway = paymentGatewayFactory.getGateway(method.getPaymentProvider());
        gateway.deletePaymentMethod(method.getExternalMethodId());

        paymentMethodRepository.delete(method);

        auditService.log(method.getUserId(), "PAYMENT_METHOD_DELETED",
                "payment_method", methodId.toString(), null);

        log.info("Payment method deleted: userId={} methodId={}", method.getUserId(), methodId);
    }

    PaymentMethodResponse toResponse(PaymentMethod m) {
        PaymentMethodResponse r = new PaymentMethodResponse();
        r.setId(m.getId());
        r.setUserId(m.getUserId());
        r.setPaymentProvider(m.getPaymentProvider());
        r.setCardLastFour(m.getCardLastFour());
        r.setCardBrand(m.getCardBrand());
        r.setDefault(m.isDefault());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
