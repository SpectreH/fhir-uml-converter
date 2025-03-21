package com.fhir.server.controller;

import com.fhir.server.service.ConverterService;
import com.fhir.server.util.BodyMediaType;
import com.fhir.server.util.Config;
import com.fhir.server.util.ContentDispositionType;
import com.fhir.server.util.ViewMode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ConverterController {
    private final ConverterService converterService;

    public ConverterController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping("/fhir2uml")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> convertFhir2Uml(
            @RequestBody String body,
            @RequestHeader HttpHeaders httpHeaders
    ) throws IOException, InterruptedException {
        List<MediaType> acceptList = httpHeaders.getAccept();

        ViewMode viewMode = ViewMode.fromMediaTypes(acceptList);
        BodyMediaType bodyMediaType = BodyMediaType.fromMediaTypes(acceptList);

        String cdHeader = httpHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        ContentDispositionType.ParsedContentDisposition parsedCd = ContentDispositionType.parse(cdHeader);

        String requestedContentType = httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        String imageType = "png";
        String contentType = "text/plain";
        if (requestedContentType != null) {
            if (requestedContentType.contains("image/png")) {
                imageType = "png";
                contentType = "image/png";
            } else if (requestedContentType.contains("image/svg+xml")) {
                imageType = "svg";
                contentType = "image/svg+xml";
            }
        }

        String finalContentDisposition = parsedCd.toHeaderValue();

        String hideRemovedObjectsHeader = httpHeaders.getFirst("X-Hide-Removed-Objects");
        boolean hideRemovedObjects = Boolean.parseBoolean(hideRemovedObjectsHeader);

        String showConstraintsHeader = httpHeaders.getFirst("X-Show-Constraints");
        boolean showConstraints = Boolean.parseBoolean(showConstraintsHeader);

        String showBindingsHeader = httpHeaders.getFirst("X-Show-Bindings");
        boolean showBindings = Boolean.parseBoolean(showBindingsHeader);

        String reduceSliceClassesHeader = httpHeaders.getFirst("X-Reduce-Slice-Classes");
        boolean reduceSliceClasses = Boolean.parseBoolean(reduceSliceClassesHeader);

        String hideLegendHeader = httpHeaders.getFirst("X-Hide-Legend");
        boolean hideLegend = Boolean.parseBoolean(hideLegendHeader);

        Config config = new Config(imageType, contentType, viewMode.getViewValue(), "uml", finalContentDisposition, hideRemovedObjects, showConstraints, showBindings, reduceSliceClasses, hideLegend);

        byte[] responseBytes = converterService.convertFhirToUml(body, config);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, finalContentDisposition)
                .body(responseBytes);
    }
}
