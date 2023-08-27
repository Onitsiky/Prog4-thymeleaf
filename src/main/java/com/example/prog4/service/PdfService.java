package com.example.prog4.service;

import com.example.prog4.config.CompanyConf;
import com.example.prog4.repository.entity.Employee;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

  private String parseTemplate(Employee employee) {
    CompanyConf companyConf = new CompanyConf();
    log.info("Employee: {}", employee.getRegistrationNumber());

    Context context = new Context();
    context.setVariable("employee", employee);
    context.setVariable("company", companyConf);

    return templateEngine.process("employee_pdf_template", context);
  }

  public byte[] generatePdf(String id) {
    Employee employee = service.getOne(id);
    String htmlTemplate = parseTemplate(employee);
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
