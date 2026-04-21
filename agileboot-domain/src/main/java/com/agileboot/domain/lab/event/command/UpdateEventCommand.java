package com.agileboot.domain.lab.event.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "更新活动命令")
public class UpdateEventCommand extends CreateEventCommand {
}
