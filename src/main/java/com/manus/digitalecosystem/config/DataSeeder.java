package com.manus.digitalecosystem.config;

import com.manus.digitalecosystem.model.Company;
import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.UniversityVisibility;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.model.enums.VerificationStatus;
import com.manus.digitalecosystem.repository.CompanyRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository,
                      UniversityRepository universityRepository,
                      CompanyRepository companyRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.universityRepository = universityRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedSuperAdmin();
        seedUniversities();
        seedCompanies();
    }

    private void seedSuperAdmin() {
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            User admin = User.builder()
                    .fullName("System Administrator")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.SUPER_ADMIN)
                    .status(Status.ACTIVE)
                    .build();
            userRepository.save(admin);
        }
    }

    private void seedUniversities() {
        Set<String> existingNames = universityRepository.findAll().stream()
                .map(University::getName)
                .map(name -> name != null ? name.getEn() : null)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toSet());

        List<University> universitiesToCreate = getSeedUniversities().stream()
                .filter(university -> !existingNames.contains(university.getName().getEn()))
                .toList();

        if (!universitiesToCreate.isEmpty()) {
            universityRepository.saveAll(universitiesToCreate);
        }
    }

    private void seedCompanies() {
        Set<String> existingNames = companyRepository.findAll().stream()
                .map(Company::getName)
                .map(name -> name != null ? name.getEn() : null)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toSet());

        List<Company> companiesToCreate = getSeedCompanies().stream()
                .filter(company -> !existingNames.contains(company.getName().getEn()))
                .toList();

        if (!companiesToCreate.isEmpty()) {
            companyRepository.saveAll(companiesToCreate);
        }
    }

    private List<University> getSeedUniversities() {
        return List.of(
                university("Northbridge University", "A leading institution focused on engineering, business, and applied sciences.", "1 University Avenue, Kabul", UniversityVisibility.PUBLIC, "https://northbridge.edu", "+93 700 100 001", "info@northbridge.edu", "university-admin-01"),
                university("Riverside Institute of Technology", "Hands-on technical education for the next generation of builders.", "22 Technology Road, Herat", UniversityVisibility.PUBLIC, "https://riverside-tech.edu", "+93 700 100 002", "contact@riverside-tech.edu", "university-admin-02"),
                university("Summit National University", "A comprehensive university with strong research and innovation programs.", "88 Summit Street, Mazar-i-Sharif", UniversityVisibility.PUBLIC, "https://summitnu.edu", "+93 700 100 003", "admissions@summitnu.edu", "university-admin-03"),
                university("Pioneer University", "Known for science, education, and community impact.", "14 Pioneer Boulevard, Jalalabad", UniversityVisibility.PUBLIC, "https://pioneeru.edu", "+93 700 100 004", "hello@pioneeru.edu", "university-admin-04"),
                university("Crescent Valley University", "A modern campus offering multidisciplinary academic programs.", "7 Crescent Lane, Kandahar", UniversityVisibility.PRIVATE, "https://crescentvalley.edu", "+93 700 100 005", "office@crescentvalley.edu", "university-admin-05"),
                university("Atlas University", "Focused on research, technology transfer, and international collaboration.", "50 Atlas Road, Kabul", UniversityVisibility.PUBLIC, "https://atlasuniversity.edu", "+93 700 100 006", "info@atlasuniversity.edu", "university-admin-06"),
                university("Evergreen College", "A student-centered college with strong arts and computing departments.", "9 Evergreen Avenue, Bamyan", UniversityVisibility.PUBLIC, "https://evergreencollege.edu", "+93 700 100 007", "support@evergreencollege.edu", "university-admin-07"),
                university("Skyline University", "Prepares graduates for careers in business, public policy, and engineering.", "101 Skyline Drive, Kabul", UniversityVisibility.PRIVATE, "https://skylineuniversity.edu", "+93 700 100 008", "info@skylineuniversity.edu", "university-admin-08"),
                university("Grand Horizon University", "Committed to academic excellence and practical learning.", "66 Horizon Street, Herat", UniversityVisibility.PUBLIC, "https://grandhorizon.edu", "+93 700 100 009", "contact@grandhorizon.edu", "university-admin-09"),
                university("Blue Ridge Institute", "Offers career-oriented degrees with a strong internship culture.", "18 Blue Ridge Road, Balkh", UniversityVisibility.PUBLIC, "https://blueridgeinstitute.edu", "+93 700 100 010", "hello@blueridgeinstitute.edu", "university-admin-10"),
                university("Vertex University", "A research-forward university supporting innovation and entrepreneurship.", "31 Vertex Plaza, Kabul", UniversityVisibility.PUBLIC, "https://vertexu.edu", "+93 700 100 011", "info@vertexu.edu", "university-admin-11"),
                university("Maple Leaf University", "Known for inclusive teaching and modern student services.", "44 Maple Street, Mazar-i-Sharif", UniversityVisibility.PRIVATE, "https://mapleleaf.edu", "+93 700 100 012", "admissions@mapleleaf.edu", "university-admin-12"),
                university("Aurora University", "A growing campus with strong programs in health and computing.", "73 Aurora Road, Kabul", UniversityVisibility.PUBLIC, "https://aurorauniversity.edu", "+93 700 100 013", "office@aurorauniversity.edu", "university-admin-13"),
                university("Cedar Heights University", "Dedicated to teaching, research, and professional development.", "55 Cedar Heights, Herat", UniversityVisibility.PUBLIC, "https://cedarheights.edu", "+93 700 100 014", "contact@cedarheights.edu", "university-admin-14"),
                university("Falcon Peak University", "Offers practical programs aligned with regional industry needs.", "27 Falcon Peak Avenue, Kandahar", UniversityVisibility.PRIVATE, "https://falconpeak.edu", "+93 700 100 015", "info@falconpeak.edu", "university-admin-15"),
                university("Golden Gate University", "A dynamic university emphasizing leadership and innovation.", "90 Golden Gate Road, Kabul", UniversityVisibility.PUBLIC, "https://goldengateu.edu", "+93 700 100 016", "hello@goldengateu.edu", "university-admin-16"),
                university("Harbor View Institute", "A compact institute with strong applied science pathways.", "12 Harbor View, Herat", UniversityVisibility.PUBLIC, "https://harborviewinstitute.edu", "+93 700 100 017", "support@harborviewinstitute.edu", "university-admin-17"),
                university("Liberty University", "Supports academic freedom, student leadership, and public service.", "11 Liberty Street, Kabul", UniversityVisibility.PRIVATE, "https://libertyuniversity.edu", "+93 700 100 018", "info@libertyuniversity.edu", "university-admin-18"),
                university("Meadow Brook University", "A balanced university with programs across science and humanities.", "6 Meadow Brook Lane, Bamyan", UniversityVisibility.PUBLIC, "https://meadowbrook.edu", "+93 700 100 019", "admissions@meadowbrook.edu", "university-admin-19"),
                university("Unity International University", "An internationally oriented university for diverse learners.", "2 Unity Square, Kabul", UniversityVisibility.PUBLIC, "https://unityinternational.edu", "+93 700 100 020", "contact@unityinternational.edu", "university-admin-20")
        );
    }

    private List<Company> getSeedCompanies() {
        return List.of(
                company("Apex Software Labs", "Builds custom software products for startups and enterprises.", 120, "Product Development", "https://apexsoftwarelabs.com", "+93 750 200 001", "hello@apexsoftwarelabs.com", "company-admin-01"),
                company("BluePeak Solutions", "Delivers cloud, web, and mobile engineering services.", 85, "Service Delivery", "https://bluepeaksolutions.com", "+93 750 200 002", "info@bluepeaksolutions.com", "company-admin-02"),
                company("CoreLink Systems", "Creates reliable enterprise platforms and integrations.", 210, "Enterprise Software", "https://corelinksystems.com", "+93 750 200 003", "contact@corelinksystems.com", "company-admin-03"),
                company("NovaByte Studio", "Designs polished digital products with a user-first approach.", 54, "Digital Product", "https://novabytestudio.com", "+93 750 200 004", "team@novabytestudio.com", "company-admin-04"),
                company("Vertex Cloud Services", "Supports organizations with cloud migration and managed services.", 140, "Cloud Services", "https://vertexcloudservices.com", "+93 750 200 005", "support@vertexcloudservices.com", "company-admin-05"),
                company("Summit Data Group", "Specializes in analytics, dashboards, and reporting systems.", 95, "Data Analytics", "https://summitdatagroup.com", "+93 750 200 006", "hello@summitdatagroup.com", "company-admin-06"),
                company("Orion Labs", "A research-driven software company focused on applied AI.", 160, "AI Development", "https://orionlabs.com", "+93 750 200 007", "info@orionlabs.com", "company-admin-07"),
                company("GreenField Technologies", "Builds internal tools and automation platforms for teams.", 68, "Automation", "https://greenfieldtech.com", "+93 750 200 008", "contact@greenfieldtech.com", "company-admin-08"),
                company("Frontier Digital", "Provides product strategy, design, and implementation services.", 110, "Product Consulting", "https://frontierdigital.com", "+93 750 200 009", "hello@frontierdigital.com", "company-admin-09"),
                company("Prime Logic Works", "Develops secure back-end systems and business applications.", 180, "Software Engineering", "https://primelogicworks.com", "+93 750 200 010", "support@primelogicworks.com", "company-admin-10"),
                company("Skyward Apps", "Builds mobile-first platforms for education and commerce.", 72, "Mobile Development", "https://skywardapps.com", "+93 750 200 011", "info@skywardapps.com", "company-admin-11"),
                company("CedarByte Systems", "Offers implementation, support, and modernization services.", 130, "IT Services", "https://cedarbytesystems.com", "+93 750 200 012", "contact@cedarbytesystems.com", "company-admin-12"),
                company("Nimbus Innovations", "Turns product ideas into usable software and prototypes.", 45, "Innovation Lab", "https://nimbusinnovations.com", "+93 750 200 013", "team@nimbusinnovations.com", "company-admin-13"),
                company("MetroTech Dynamics", "Builds scalable tools for operations and logistics.", 205, "Platform Engineering", "https://metrotechdynamics.com", "+93 750 200 014", "info@metrotechdynamics.com", "company-admin-14"),
                company("Pioneer Software House", "Delivers full-stack development and maintenance services.", 98, "Software House", "https://pioneersoftwarehouse.com", "+93 750 200 015", "hello@pioneersoftwarehouse.com", "company-admin-15"),
                company("Aurora Commerce Tech", "Creates digital commerce solutions for growing businesses.", 155, "E-commerce", "https://auroracommercetech.com", "+93 750 200 016", "support@auroracommercetech.com", "company-admin-16"),
                company("Falcon Security Labs", "Focuses on application security and infrastructure hardening.", 77, "Cybersecurity", "https://falconsecuritylabs.com", "+93 750 200 017", "contact@falconsecuritylabs.com", "company-admin-17"),
                company("Harbor Digital Group", "Builds internal platforms and customer-facing applications.", 145, "Digital Transformation", "https://harbordigitalgroup.com", "+93 750 200 018", "info@harbordigitalgroup.com", "company-admin-18"),
                company("Maple Works", "A small studio shipping elegant software for modern teams.", 33, "Studio", "https://mapleworks.com", "+93 750 200 019", "hello@mapleworks.com", "company-admin-19"),
                company("Unity Engineering Corp", "Supports organizations with architecture, development, and delivery.", 230, "Engineering Services", "https://unityengineeringcorp.com", "+93 750 200 020", "contact@unityengineeringcorp.com", "company-admin-20")
        );
    }

    private University university(String name,
                                  String description,
                                  String address,
                                  UniversityVisibility visibility,
                                  String website,
                                  String phone,
                                  String email,
                                  String adminUserId) {
        return University.builder()
                .name(localizedText(name))
                .description(localizedText(description))
                .address(localizedText(address))
                .visibility(visibility)
                .website(website)
                .phone(phone)
                .email(email)
                .adminUserId(adminUserId)
                .verificationStatus(VerificationStatus.APPROVED)
                .build();
    }

    private Company company(String name,
                            String description,
                            int numberOfEmployees,
                            String developmentType,
                            String website,
                            String phone,
                            String email,
                            String adminUserId) {
        return Company.builder()
                .name(localizedText(name))
                .description(localizedText(description))
                .numberOfEmployees(numberOfEmployees)
                .developmentType(localizedText(developmentType))
                .website(website)
                .phone(phone)
                .email(email)
                .adminUserId(adminUserId)
                .verificationStatus(VerificationStatus.APPROVED)
                .build();
    }

    private LocalizedText localizedText(String value) {
        return LocalizedText.builder()
                .en(value)
                .fa(value)
                .ps(value)
                .build();
    }
}
