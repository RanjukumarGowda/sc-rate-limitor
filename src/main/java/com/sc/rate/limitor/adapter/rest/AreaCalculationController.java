package com.sc.rate.limitor.adapter.rest;

import com.sc.rate.limitor.domain.dto.in.RectangleDimension;
import com.sc.rate.limitor.domain.dto.in.TriangleDimension;
import com.sc.rate.limitor.domain.dto.out.Area;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/area", consumes = MediaType.APPLICATION_JSON_VALUE)
class AreaCalculationController {

    @PostMapping(value = "/rectangle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Area> rectangle(@RequestBody RectangleDimension dimensions) {
        log.info("Received request to calculate are of rectangle and dimensions are {}", dimensions);
        var rectangleArea = Area.builder()
                .shape("rectangle").area(dimensions.getLength() * dimensions.getWidth()).build();
        return ResponseEntity.ok(rectangleArea);
    }

    @PostMapping(value = "/triangle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Area> triangle(@RequestBody TriangleDimension dimensions) {
        log.info("Received request to calculate are of triangle and dimensions are {}", dimensions);
        var triangleArea = Area.builder()
                .shape("triangle").area(0.5d * dimensions.getHeight() * dimensions.getBase()).build();
        return ResponseEntity.ok(triangleArea);
    }
}
