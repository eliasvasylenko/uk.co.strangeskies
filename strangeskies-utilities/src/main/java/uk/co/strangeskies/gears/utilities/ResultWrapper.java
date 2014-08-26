package uk.co.strangeskies.gears.utilities;

public class ResultWrapper<ResultType> {
  private ResultType result;

  public ResultWrapper() {
    result = null;
  }

  public ResultWrapper(ResultType result) {
    this.result = result;
  }

  public void setResult(ResultType result) {
    this.result = result;
  }

  public ResultType getResult() {
    return result;
  }
}
