package fr.iolabs.leaf.redirect.redirection;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/redirections")
public class LeafRedirectController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafRedirectionRepository redirectionRepository;

	@Autowired
	private LeafRedirectionCreationBatchRepository redirectionCreationBatchRepository;

	@CrossOrigin
	@AdminOnly
	@GetMapping("/batches")
	public List<LeafRedirectionCreationBatch> listAllRedirectionCreationBatches() {
		return this.redirectionCreationBatchRepository.findAll();
	}

	@CrossOrigin
	@AdminOnly
	@GetMapping(path = "/batches/{id}")
	public LeafRedirectionCreationBatch findRedirectionCreationBatchById(@PathVariable String id) {
		Optional<LeafRedirectionCreationBatch> redirectionCreationBatch = this.redirectionCreationBatchRepository
				.findById(id);
		if (!redirectionCreationBatch.isPresent()) {
			throw new NotFoundException();
		}
		return redirectionCreationBatch.get();
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/batches")
	public LeafRedirectionCreationBatch createRedirects(
			@RequestBody LeafRedirectionCreationBatch redirectionCreationBatch) {
		if (Strings.isBlank(redirectionCreationBatch.getComment())) {
			throw new BadRequestException("Batch must contain a comment");
		}
		if (redirectionCreationBatch.getSize() <= 0) {
			throw new BadRequestException("Batch must contain a positive non-null size");
		}
		LeafRedirectionCreationBatch previousBatch = this.redirectionCreationBatchRepository
				.findFirstByOrderByEndAtDesc();
		long lastId = previousBatch != null ? previousBatch.getEndAt() : 0;

		long startAt = lastId + 1;
		long endAt = startAt + redirectionCreationBatch.getSize() - 1;

		redirectionCreationBatch.setStartAt(startAt);
		redirectionCreationBatch.setEndAt(endAt);

		redirectionCreationBatch.setCreatorId(this.coreContext.getAccount().getId());

		redirectionCreationBatch.getMetadata();

		LeafRedirectionCreationBatch savedBatch = this.redirectionCreationBatchRepository
				.insert(redirectionCreationBatch);

		List<LeafRedirection> redirections = new ArrayList<>();
		for (long redirectionId = startAt; redirectionId <= endAt; redirectionId++) {
			LeafRedirection redirection = new LeafRedirection();
			redirection.setId(redirectionId);
			redirection.setCreationBatchId(savedBatch.getId());
			redirections.add(redirection);
		}
		this.redirectionRepository.saveAll(redirections);

		return savedBatch;
	}

	@CrossOrigin
	@AdminOnly
	@GetMapping
	public List<LeafRedirection> listAllRedirections(@RequestParam(name = "batchId", required = true) String batchId) {
		return this.redirectionRepository.findAllByCreationBatchId(batchId);
	}

	@CrossOrigin
	@PermitAll
	@GetMapping("/{redirectionId}")
	public LeafRedirection findById(@PathVariable(name = "redirectionId") String redirectionId,
			@RequestParam(name = "hex", defaultValue = "true") boolean hex) {
		Long id = null;
		if (hex) {
			if (redirectionId.matches("^[0-9a-fA-F]+$")) {
				id = Long.parseLong(redirectionId, 16);
			}
		} else {
			if (redirectionId.matches("^[0-9]+$")) {
				id = Long.parseLong(redirectionId, 10);
			}
		}
		if (id == null) {
			throw new BadRequestException("Invalid id format: " + redirectionId);
		}
		Optional<LeafRedirection> optRedirection = this.redirectionRepository.findById(id);
		if (optRedirection.isEmpty()) {
			throw new NotFoundException("No redirection with id: " + id);
		}
		return optRedirection.get();
	}

	@CrossOrigin
	@AdminOnly
	@PatchMapping("/{redirectionId}")
	public LeafRedirection updateById(@PathVariable(name = "redirectionId") String redirectionId,
			@RequestParam(name = "hex", defaultValue = "true") boolean hex, @RequestBody LeafRedirection updates) {
		LeafRedirection redirection = this.findById(redirectionId, hex);
		redirection.setRedirectUrl(updates.getRedirectUrl());
		return this.redirectionRepository.save(redirection);
	}

}
