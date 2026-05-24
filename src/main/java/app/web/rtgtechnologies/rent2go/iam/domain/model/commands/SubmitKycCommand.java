package app.web.rtgtechnologies.rent2go.iam.domain.model.commands;

public record SubmitKycCommand(Long userId, String fullName, String idNumber, String dniFrontUrl, String dniBackUrl, String driverLicenseUrl) {}
