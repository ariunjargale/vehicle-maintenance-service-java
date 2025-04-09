package ca.humber.util;

import ca.humber.model.Service;
import ca.humber.model.ServiceModel;

public class ServiceMapper {

    public static ServiceModel toModel(Service s) {
        return new ServiceModel(
                s.getServiceId(),
                s.getServiceName(),
                Integer.parseInt(s.getServiceTypeId()), // Convert to int
                s.getPrice(),
                s.getIsActive() != null && s.getIsActive()
        );
    }

    public static Service toEntity(ServiceModel m) {
        return new Service(
                m.getServiceId(),
                m.getServiceName(),
                String.valueOf(m.getServiceTypeId()), // Convert back to String
                m.getPrice(),
                m.isActive()
        );
    }
}
