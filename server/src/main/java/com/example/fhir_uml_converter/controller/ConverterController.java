package com.example.fhir_uml_converter.controller;

import com.example.fhir_uml_converter.service.ConverterService;
import com.example.fhir_uml_converter.util.BodyMediaType;
import com.example.fhir_uml_converter.util.ContentDispositionType;
import com.example.fhir_uml_converter.util.ViewMode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ConverterController {
    private final ConverterService converterService;

    public ConverterController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping("/fhir2uml")
    public ResponseEntity<?> convertFhir2Uml(
            @RequestBody String body,
            @RequestHeader HttpHeaders httpHeaders
    ) throws IOException, InterruptedException {
        List<MediaType> acceptList = httpHeaders.getAccept();

        ViewMode viewMode = ViewMode.fromMediaTypes(acceptList);
        BodyMediaType bodyMediaType = BodyMediaType.fromMediaTypes(acceptList);

        String requestedContentType = httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        if (requestedContentType == null) {
            requestedContentType = "text/plain";
        }

        String cdHeader = httpHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        ContentDispositionType.ParsedContentDisposition parsedCd = ContentDispositionType.parse(cdHeader);

        String responseMediaType = requestedContentType.contains("image/png")
                ? MediaType.IMAGE_PNG_VALUE
                : MediaType.TEXT_PLAIN_VALUE;

        String finalContentDisposition = parsedCd.toHeaderValue();

        byte[] responseBytes = converterService.convertFhirToUml(body, viewMode, responseMediaType);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, responseMediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, finalContentDisposition)
                .body(responseBytes);
    }

    @PostMapping("/uml2fhir")
    public String convertUml2Fhir(@RequestBody String uml) {
        return converterService.convertUmlToFhir(uml);
    }
}
