package nextstep.subway.domain;

import nextstep.subway.exception.DeleteSectionException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Embeddable
public class Sections {
    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public void add(Section section) {
        sections.add(section);
    }

    public void remove(long lastDownStationId) {
        Station lastDownStation = getLastDownStation();

        if (!isAvailableDelete()) {
            throw new DeleteSectionException("구간이 1개 이하인 경우 역을 삭제할 수 없습니다.");
        }

        if (!lastDownStation.isSameStation(lastDownStationId)) {
            throw new DeleteSectionException("구간에 일치하는 하행 종점역이 없습니다.");
        }

        Section delete = getByDownStation(lastDownStation);

        sections.remove(delete);
    }

    public List<Section> getAllSections() {
        return sections;
    }

    public boolean isEmpty() {
        return sections.isEmpty();
    }

    public List<Station> getSectionStations(Function<Section, Station> func) {
        return sections
            .stream()
            .map(func)
            .collect(Collectors.toList());
    }

    public Station getLastDownStation() {
        return sections
            .stream()
            .map(Section::getDownStation)
            .reduce((a, b) -> b)
            .orElseThrow(() -> new NoSuchElementException("하행 종점역이 없습니다."));
    }

    public boolean isAvailableDelete() {
        return sections.size() > 1;
    }

    public Section getByDownStation(Station station) {
        return sections.stream()
            .filter(section -> section.getDownStation() == station)
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("일치하는 구간을 찾을 수 없습니다."));
    }

    public List<Station> getAllStations() {
        List<Station> allStations = sections.
            stream()
            .map(Section::getUpStation)
            .collect(Collectors.toList());

        allStations.add(getLastDownStation());

        return allStations;
    }
}