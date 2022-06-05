package fr.iolabs.leaf.messenger.rooms;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.common.errors.UnauthorizedException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/rooms")
public class LeafRoomController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private LeafAccountRepository accountRepository;

	@CrossOrigin
	@GetMapping
	public List<Room> listAllRooms() {
		return this.roomRepository.findAll();
	}

	@CrossOrigin
	@GetMapping(path = "/{id}")
	public Room findRoomById(@PathVariable String id) {
		Optional<Room> room = this.roomRepository.findById(id);
		if (!room.isPresent()) {
			throw new NotFoundException();
		}
		return room.get();
	}

	@CrossOrigin
	@PostMapping
	public Room createRoom(@RequestBody Room from) {
		if (Strings.isBlank(from.getName())) {
			throw new BadRequestException("room's name cannot be empty");
		}

		Room room = new Room();
		room.setName(from.getName());

		String ownerId = this.coreContext.getAccount().getId();
		HashSet<String> memberIds = new HashSet<>();
		memberIds.add(ownerId);
		for (LeafAccount account : this.accountRepository.findAllById(from.getMembers())) {
			memberIds.add(account.getId());
		}
		room.setMembers(memberIds);

		room.setOwnerId(ownerId);
		room.getMembers().add(coreContext.getAccount().getId());
		room.setMessages(new ArrayList<>());
		room.getMetadata().updateLastModification();

		return this.roomRepository.insert(room);
	}

	@CrossOrigin
	@PatchMapping(path = "/{id}")
	public Room updateRoom(@PathVariable String id, @RequestBody Room from) {
		Room room = this.findRoomById(id);
		this.checkOwnership(room, coreContext.getAccount().getId());
		if (!Strings.isBlank(from.getName())) {
			room.setName(from.getName());
			room.getMetadata().updateLastModification();
		}
		return this.roomRepository.save(room);
	}

	@CrossOrigin
	@DeleteMapping(path = "/{id}")
	public void deleteRoom(@PathVariable String id) {
		Room room = this.findRoomById(id);
		this.checkOwnership(room, coreContext.getAccount().getId());
		this.roomRepository.delete(room);
	}

	public void checkOwnership(Ownable ownable, String accountId) {
		if (accountId == null || !accountId.equals(ownable.getOwnerId())) {
			throw new UnauthorizedException();
		}
	}
}
