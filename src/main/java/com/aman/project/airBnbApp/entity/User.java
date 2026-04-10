package com.aman.project.airBnbApp.entity;

import com.aman.project.airBnbApp.entity.enums.Role;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String password; // encode it later bcrypt

	private String name;

	@ElementCollection(fetch = FetchType.EAGER) // ?
	@Enumerated(EnumType.STRING)
	private Set<Role> roles;

	// -- spring security
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		//return List.of();
		return roles
			.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
			.collect(Collectors.toSet());
	} // ques: why "ROLE_" and why this steam and what is SimpleGrantedAuthority

	@Override
	public String getUsername() {
		return email;
	}

	//
	//    @OneToMany(mappedBy = "user")
	//    private Set<Guest> guests;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof User user)) return false;
		return Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
