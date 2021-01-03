package com.pim.mum;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.pim.mum.sentences.SentenceGenerator;

@Controller
public class ThymeleafController {

	private static final Logger logger = LoggerFactory.getLogger(ThymeleafController.class);

	@Autowired
	private SentenceGenerator sentenceGenerator;

	@GetMapping("/")
	public ModelAndView mum(final HttpSession session) {
		final Object removedSentence = session.getAttribute("sentence5");
		final String newSentence = sentenceGenerator.generateSentence();

		// Store the new sentence in the session, so that it is available during the next call to this method:
		session.setAttribute("sentence5", session.getAttribute("sentence4"));
		session.setAttribute("sentence4", session.getAttribute("sentence3"));
		session.setAttribute("sentence3", session.getAttribute("sentence2"));
		session.setAttribute("sentence2", session.getAttribute("sentence1"));
		session.setAttribute("sentence1", newSentence);

		// Fill the model with the data that the Thymeleaf template expects:
		final ModelAndView mav = new ModelAndView("mum");
		mav.addObject("sentence1", session.getAttribute("sentence1"));
		mav.addObject("sentence2", session.getAttribute("sentence2"));
		mav.addObject("sentence3", session.getAttribute("sentence3"));
		mav.addObject("sentence4", session.getAttribute("sentence4"));
		mav.addObject("sentence5", session.getAttribute("sentence5"));
		mav.addObject("sentence6", removedSentence);

		logger.info(newSentence);

		return mav;
	}
}
