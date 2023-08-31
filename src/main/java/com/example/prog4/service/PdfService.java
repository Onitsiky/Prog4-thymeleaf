package com.example.prog4.service;

import com.example.prog4.config.CompanyConf;
import com.example.prog4.model.enums.AgeType;
import com.example.prog4.model.exception.BadRequestException;
import com.example.prog4.repository.entity.Employee;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
@AllArgsConstructor
@Slf4j
public class PdfService {
  private final EmployeeService service;
  private final TemplateEngine templateEngine;

  private String parseTemplate(Employee employee, AgeType ageType) {
    CompanyConf companyConf = new CompanyConf();
    int years = calculateEmployeeYear(employee, ageType);

    Context context = new Context();
    context.setVariable("employee", employee);
    context.setVariable("age", years);
    context.setVariable("company", companyConf);

    return templateEngine.process("employee_pdf_template", context);
  }

  private static int calculateEmployeeYear(Employee employee, AgeType ageType) {
    if(ageType.equals(AgeType.BIRTHDAY)) {
      Period period = Period.between(employee.getBirthDate(), LocalDate.now());
      return period.getYears();
    } else if(ageType.equals(AgeType.YEAR_ONLY)) {
      return LocalDate.now().getYear() - employee.getBirthDate().getYear();
    }
    throw new BadRequestException("The age type entered is not valid. Possible values are: \"YEAR_ONLY\" or \"BIRTHDAY\".");
  }

  public byte[] generatePdf(String id, AgeType ageType) {
    Employee employee = service.getOne(id);
    String htmlTemplate = parseTemplate(employee, ageType);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ITextRenderer renderer = new ITextRenderer();

    try {
      renderer.setDocumentFromString(htmlTemplate);
      renderer.layout();
      renderer.createPDF(outputStream);
    } catch (DocumentException e) {
      throw new RuntimeException("An error occurred when creating the pdf document", e);
    }
    return outputStream.toByteArray();
  }
}
