package com.aman.project.airBnbApp.util;

import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.exception.UnAuthorisedException;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {

	public static User getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!(principal instanceof User)) {
			throw new UnAuthorisedException("User is not authenticated");
		}
		return (User) principal;
	}
}
