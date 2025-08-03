package Generations.toDoListConTokenBuono.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class Task {
	@NotNull
	@Positive
	private int id;
	@NotBlank
	private String title;
	@Size(max=250)
	private String description;
	@NotNull
	private int priority;
	@NotBlank
	private String status;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Task(@NotNull @Positive int id, @NotBlank String title, @Size(max = 250) String description,
			@NotNull int priority, @NotBlank String status) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.priority = priority;
		this.status = status;
	}
	public Task() {
		super();
	}
	@Override
	public String toString() {
		return "Task [id=" + id + ", title=" + title + ", description=" + description + ", priority=" + priority
				+ ", status=" + status + "]";
	}

}
