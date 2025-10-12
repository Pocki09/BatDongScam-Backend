package com.se100.bds.helpers.domains;

import com.se100.bds.entities.location.City;
import com.se100.bds.entities.location.District;
import com.se100.bds.entities.location.Ward;
import com.se100.bds.repositories.location.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LocationDummyData {

    private final CityRepository cityRepository;

    public void createDummy() {
        if (any()) {
            log.info("Locations already exist, skipping dummy data creation");
            return;
        }
        createDummyLocations();
    }

    public boolean any() {
        return cityRepository.count() > 0;
    }

    private void createDummyLocations() {
        log.info("Creating dummy location data");

        List<City> cities = new ArrayList<>();

        // Create Ho Chi Minh City with districts and wards
        City hcmc = createCity("Ho Chi Minh City", "The largest city in Vietnam",
                new BigDecimal("2095"), new BigDecimal("150000000"), 9000000);

        // District 1 - Central Business District
        District district1 = createDistrict(hcmc, "District 1",
                new BigDecimal("7.73"), new BigDecimal("250000000"), 204000);
        district1.getWards().add(createWard(district1, "Ben Nghe Ward", new BigDecimal("0.89"), new BigDecimal("280000000"), 18000));
        district1.getWards().add(createWard(district1, "Ben Thanh Ward", new BigDecimal("0.64"), new BigDecimal("270000000"), 15000));
        district1.getWards().add(createWard(district1, "Nguyen Thai Binh Ward", new BigDecimal("0.52"), new BigDecimal("260000000"), 12000));
        hcmc.getDistricts().add(district1);

        // District 2 - Modern residential area
        District district2 = createDistrict(hcmc, "District 2",
                new BigDecimal("49.75"), new BigDecimal("180000000"), 400000);
        district2.getWards().add(createWard(district2, "Thao Dien Ward", new BigDecimal("3.2"), new BigDecimal("220000000"), 25000));
        district2.getWards().add(createWard(district2, "An Phu Ward", new BigDecimal("4.5"), new BigDecimal("210000000"), 35000));
        district2.getWards().add(createWard(district2, "Binh An Ward", new BigDecimal("2.8"), new BigDecimal("200000000"), 22000));
        hcmc.getDistricts().add(district2);

        // District 3
        District district3 = createDistrict(hcmc, "District 3",
                new BigDecimal("4.90"), new BigDecimal("220000000"), 188000);
        district3.getWards().add(createWard(district3, "Ward 1", new BigDecimal("0.45"), new BigDecimal("230000000"), 15000));
        district3.getWards().add(createWard(district3, "Ward 2", new BigDecimal("0.38"), new BigDecimal("225000000"), 12000));
        district3.getWards().add(createWard(district3, "Ward 3", new BigDecimal("0.42"), new BigDecimal("228000000"), 14000));
        hcmc.getDistricts().add(district3);

        // District 7 - Wealthy residential area
        District district7 = createDistrict(hcmc, "District 7",
                new BigDecimal("35.76"), new BigDecimal("160000000"), 350000);
        district7.getWards().add(createWard(district7, "Tan Phong Ward", new BigDecimal("2.1"), new BigDecimal("170000000"), 28000));
        district7.getWards().add(createWard(district7, "Tan Phu Ward", new BigDecimal("3.8"), new BigDecimal("165000000"), 42000));
        district7.getWards().add(createWard(district7, "Phu My Ward", new BigDecimal("4.2"), new BigDecimal("175000000"), 45000));
        hcmc.getDistricts().add(district7);

        // Binh Thanh District
        District binhThanh = createDistrict(hcmc, "Binh Thanh District",
                new BigDecimal("20.76"), new BigDecimal("140000000"), 450000);
        binhThanh.getWards().add(createWard(binhThanh, "Ward 1", new BigDecimal("0.75"), new BigDecimal("150000000"), 18000));
        binhThanh.getWards().add(createWard(binhThanh, "Ward 2", new BigDecimal("0.68"), new BigDecimal("148000000"), 16000));
        binhThanh.getWards().add(createWard(binhThanh, "Ward 3", new BigDecimal("0.82"), new BigDecimal("152000000"), 20000));
        hcmc.getDistricts().add(binhThanh);

        cities.add(hcmc);

        // Create Hanoi
        City hanoi = createCity("Hanoi", "The capital city of Vietnam",
                new BigDecimal("3359"), new BigDecimal("120000000"), 8000000);

        District badinh = createDistrict(hanoi, "Ba Dinh District",
                new BigDecimal("9.21"), new BigDecimal("180000000"), 221000);
        badinh.getWards().add(createWard(badinh, "Dien Bien Ward", new BigDecimal("0.42"), new BigDecimal("190000000"), 12000));
        badinh.getWards().add(createWard(badinh, "Doi Can Ward", new BigDecimal("0.38"), new BigDecimal("185000000"), 11000));
        hanoi.getDistricts().add(badinh);

        District hoankiem = createDistrict(hanoi, "Hoan Kiem District",
                new BigDecimal("5.29"), new BigDecimal("250000000"), 143000);
        hoankiem.getWards().add(createWard(hoankiem, "Hang Bac Ward", new BigDecimal("0.25"), new BigDecimal("280000000"), 8000));
        hoankiem.getWards().add(createWard(hoankiem, "Hang Gai Ward", new BigDecimal("0.22"), new BigDecimal("275000000"), 7000));
        hanoi.getDistricts().add(hoankiem);

        cities.add(hanoi);

        // Create Da Nang
        City danang = createCity("Da Nang", "A major port city in central Vietnam",
                new BigDecimal("1285"), new BigDecimal("80000000"), 1200000);

        District haiChau = createDistrict(danang, "Hai Chau District",
                new BigDecimal("20.88"), new BigDecimal("100000000"), 190000);
        haiChau.getWards().add(createWard(haiChau, "Thanh Binh Ward", new BigDecimal("1.2"), new BigDecimal("110000000"), 12000));
        haiChau.getWards().add(createWard(haiChau, "Thach Thang Ward", new BigDecimal("1.5"), new BigDecimal("108000000"), 15000));
        danang.getDistricts().add(haiChau);

        District sonTra = createDistrict(danang, "Son Tra District",
                new BigDecimal("60.35"), new BigDecimal("95000000"), 138000);
        sonTra.getWards().add(createWard(sonTra, "Tho Quang Ward", new BigDecimal("3.2"), new BigDecimal("120000000"), 18000));
        sonTra.getWards().add(createWard(sonTra, "Man Thai Ward", new BigDecimal("2.8"), new BigDecimal("115000000"), 15000));
        danang.getDistricts().add(sonTra);

        cities.add(danang);

        // Save all cities (cascade will save districts and wards)
        cityRepository.saveAll(cities);
        log.info("Saved {} cities with their districts and wards to database", cities.size());
    }

    private City createCity(String name, String description, BigDecimal totalArea,
                           BigDecimal avgLandPrice, Integer population) {
        return City.builder()
                .cityName(name)
                .description(description)
                .imgUrl(null)
                .totalArea(totalArea)
                .avgLandPrice(avgLandPrice)
                .population(population)
                .isActive(true)
                .districts(new ArrayList<>())
                .build();
    }

    private District createDistrict(City city, String name, BigDecimal totalArea,
                                   BigDecimal avgLandPrice, Integer population) {
        return District.builder()
                .city(city)
                .districtName(name)
                .description("District in " + city.getCityName())
                .imgUrl(null)
                .totalArea(totalArea)
                .avgLandPrice(avgLandPrice)
                .population(population)
                .isActive(true)
                .wards(new ArrayList<>())
                .build();
    }

    private Ward createWard(District district, String name, BigDecimal totalArea,
                           BigDecimal avgLandPrice, Integer population) {
        return Ward.builder()
                .district(district)
                .wardName(name)
                .description("Ward in " + district.getDistrictName())
                .imgUrl(null)
                .totalArea(totalArea)
                .avgLandPrice(avgLandPrice)
                .population(population)
                .isActive(true)
                .properties(new ArrayList<>())
                .build();
    }
}

