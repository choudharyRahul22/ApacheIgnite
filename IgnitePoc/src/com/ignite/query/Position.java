package com.ignite.query;

public class Position {

	private final Long id;

	private final Long portfolidId;

	private final String cusip;

	private final Long value;

	public Position(Long id, Long portfolidId, String cusip, Long value) {
		this.id = id;
		this.portfolidId = portfolidId;
		this.cusip = cusip;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public Long getPortfolidId() {
		return portfolidId;
	}

	public String getCusip() {
		return cusip;
	}

	public Long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Position [id=" + id + ", portfolidId=" + portfolidId + ", cusip=" + cusip + ", value=" + value + "]";
	}

	
}
