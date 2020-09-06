package com.sc.rate.limitor.adapter.rest;

import com.sc.rate.limitor.domain.dto.in.RectangleDimension;
import com.sc.rate.limitor.domain.dto.in.TriangleDimension;
import com.sc.rate.limitor.domain.dto.out.Area;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AreaCalculationControllerTest {

    private final AreaCalculationController calculationController = new AreaCalculationController();

    @Test
    public void whenValidRectangleRequest_ShouldReturnValidResponse() {
        //Given
        var rectangleDimension = RectangleDimension.builder().length(2).width(3).build();

        //When
        var response = calculationController.rectangle(rectangleDimension);

        //Then
        var rectangleArea = Area.builder().shape("rectangle").area(6.0).build();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(rectangleArea);
    }

    @Test
    public void whenValidTriangleRequest_ShouldReturnValidResponse() {
        //Given
        var triangleDimension = TriangleDimension.builder().base(2).height(3).build();

        //When
        var response = calculationController.triangle(triangleDimension);

        //Then
        var rectangleArea = Area.builder().shape("triangle").area(3.0).build();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(rectangleArea);
    }
}
