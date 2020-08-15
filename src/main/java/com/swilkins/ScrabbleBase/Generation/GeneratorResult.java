package com.swilkins.ScrabbleBase.Generation;

import com.google.common.collect.Lists;
import com.swilkins.ScrabbleBase.Board.Location.TilePlacement;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneratorResult implements Iterable<Candidate> {

  private final Set<Candidate> candidateSet;
  private List<Candidate> candidateList = null;

  private List<Candidate> getCandidateList() {
    if (this.candidateList == null) {
      this.candidateList = new ArrayList<>(this.candidateSet);
    }
    return this.candidateList;
  }

  public GeneratorResult() {
    this.candidateSet = new HashSet<>();
  }

  public GeneratorResult(Set<Candidate> candidates) {
    this.candidateSet = candidates;
  }

  public int size() {
    return this.candidateSet.size();
  }

  public boolean isEmpty() {
    return this.candidateSet.isEmpty();
  }

  public Candidate get(int index) throws IndexOutOfBoundsException {
    return this.getCandidateList().get(index).materialize();
  }

  public GeneratorResult orderBy(Comparator<Candidate> ordering) {
    if (ordering != null) {
      this.getCandidateList().sort(ordering);
    }
    return this;
  }

  public List<Object> asNewPlacementsList(Integer pageSize) {
    List<List<TilePlacement>> filtered = this.asStream().map(candidate -> candidate.getPrimary()
            .stream()
            .filter(Predicate.not(TilePlacement::getIsExisting))
            .collect(Collectors.toList())).collect(Collectors.toList());
    List<Object> resolved = new ArrayList<>(this.size());
    resolved.addAll(pageSize == null ?
            filtered :
            Lists.partition(filtered, pageSize)
    );
    return resolved;
  }

  public Set<Candidate> asSet() {
    return this.candidateSet;
  }

  public List<Object> asCandidateList(Integer pageSize) {
    List<Object> resolved = new ArrayList<>(this.size());
    resolved.addAll(pageSize == null ?
            getCandidateList() :
            Lists.partition(getCandidateList(), pageSize)
    );
    return resolved;
  }

  public List<Object> asSerializedList(Integer pageSize) {
    List<String> serialized = this.asStream().map(Candidate::toString).collect(Collectors.toList());
    List<Object> resolved = new ArrayList<>(this.size());
    resolved.addAll(pageSize == null ?
            serialized :
            Lists.partition(serialized, pageSize)
    );
    return resolved;
  }

  public Stream<Candidate> asStream() {
    return getCandidateList().stream();
  }

  @NotNull
  @Override
  public Iterator<Candidate> iterator() {
    return getCandidateList().iterator();
  }

  @Override
  public void forEach(Consumer<? super Candidate> action) {
    getCandidateList().forEach(action);
  }

  @Override
  public Spliterator<Candidate> spliterator() {
    return getCandidateList().spliterator();
  }

}
