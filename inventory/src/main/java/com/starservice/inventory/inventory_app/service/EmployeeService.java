package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.employee.AddEmployeeRequest;
import com.starservice.inventory.inventory_app.dto.employee.EmployeeResponse;
import com.starservice.inventory.inventory_app.dto.employee.UpdateEmployeeRequest;
import com.starservice.inventory.inventory_app.entity.Employee;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private static final Set<String> VALID_GENDERS =
            Set.of("MALE", "FEMALE", "OTHERS");

    public EmployeeResponse createEmployee(AddEmployeeRequest request) {

        Company company = getCompanyFromToken();

        // validation
        if (request.getEmployeeCode() == null || request.getEmployeeCode().isBlank()) {
            throw new RuntimeException("Employee code is required");
        }

        Optional<Employee> existing = employeeRepository.findByEmployeeCodeAndCompany(request.getEmployeeCode(), company);

        if (existing.isPresent()) {
            throw new RuntimeException("Employee code already exists");
        }

        String gender = null;
        if (request.getGender() != null) {
            gender = request.getGender().trim().toUpperCase();

            if (!VALID_GENDERS.contains(gender)) {
                throw new RuntimeException("Invalid gender value");
            }
        }

        Employee employee = Employee.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .employeeCode(request.getEmployeeCode())
                .gender(gender)
                .company(company)
                .role(request.getRole())
                .activeFl(true)
                .delFl(false)
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .build();

        employeeRepository.save(employee);

        return mapToResponse(employee);
    }

    private EmployeeResponse mapToResponse(Employee e) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")
                .withZone(ZoneId.of("Asia/Kolkata"));

        return EmployeeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .phone(e.getPhone())
                .employeeCode(e.getEmployeeCode())
                .role(e.getRole())
                .gender(e.getGender())
                .company(e.getCompany())
                .status(Boolean.TRUE.equals(e.getActiveFl()) ? "ACTIVE" : "INACTIVE")
                .createdDate(e.getCreatedDate() != null ? formatter.format(e.getCreatedDate()) : null)
                .updatedDate(e.getUpdatedDate() != null ? formatter.format(e.getUpdatedDate()) : null)
                .build();
    }

    private Company getCompanyFromToken() {
        return (Company) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }

    public EmployeeResponse getById(String id) {

        Company company = getCompanyFromToken();
        Employee emp = employeeRepository.findByIdAndCompanyAndDelFlFalse(id, company)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapToResponse(emp);
    }

    public EmployeeResponse updateEmployee(UpdateEmployeeRequest request) {
        try{
            Company company = getCompanyFromToken();
            String id = request.getId();

            Employee emp = employeeRepository.findByIdAndCompanyAndDelFlFalse(id, company)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            if (request.getName() != null) {
                emp.setName(request.getName());
            }

            if (request.getPhone() != null) {
                emp.setPhone(request.getPhone());
            }

            if (request.getGender() != null) {
                String gender = request.getGender().trim().toUpperCase();

                if (!Set.of("MALE", "FEMALE", "OTHERS").contains(gender)) {
                    throw new RuntimeException("Invalid gender");
                }

                emp.setGender(gender);
            }

            if(request.getRole() != null){
                emp.setRole(request.getRole());
            }

            emp.setUpdatedDate(Instant.now());

            employeeRepository.save(emp);

            return mapToResponse(emp);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }


    }

    public String deleteEmployee(String id) {

        Company company = getCompanyFromToken();
        Employee emp = employeeRepository.findByIdAndCompanyAndDelFlFalse(id, company)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        emp.setDelFl(true);
        emp.setUpdatedDate(Instant.now());
        employeeRepository.save(emp);

        return "Employee deleted successfully";
    }

    public String toggleStatus(String id) {

        Company company = getCompanyFromToken();
        Employee emp = employeeRepository.findByIdAndCompanyAndDelFlFalse(id, company)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        emp.setActiveFl(!Boolean.TRUE.equals(emp.getActiveFl()));
        emp.setUpdatedDate(Instant.now());
        employeeRepository.save(emp);

        return emp.getActiveFl() ? "Employee activated" : "Employee deactivated";
    }

    public PageResponse<EmployeeResponse> getEmployees(int pageNo, int pageSize, String search) {

        Company company = getCompanyFromToken();

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());

        Page<Employee> employeePage;

        if (search == null || search.isBlank()) {
            employeePage = employeeRepository.findByCompanyAndDelFlFalse(company, pageable);
        } else {
            employeePage = employeeRepository.searchEmployees(company, search, pageable);
        }

        List<EmployeeResponse> employees = employeePage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<EmployeeResponse>builder()
                .totalPages(employeePage.getTotalPages())
                .totalRecords(employeePage.getTotalElements())
                .response(employees)
                .build();
    }
}
