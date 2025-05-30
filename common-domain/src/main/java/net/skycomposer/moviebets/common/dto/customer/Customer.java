package net.skycomposer.moviebets.common.dto.customer;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

  private UUID id;

  private String username;

  private String fullName;

  private BigDecimal balance;
}
