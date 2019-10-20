package fr.iolabs.leaf.messenger.rooms.messages;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.messenger.rooms.Room;
import fr.iolabs.leaf.messenger.rooms.LeafRoomController;
import fr.iolabs.leaf.messenger.rooms.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping(path="/api/rooms/{roomId}")
public class LeafMessageController {
    private static final Random random = new Random();

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private LeafRoomController roomController;

    @Autowired
    private RoomRepository roomRepository;

    @CrossOrigin
    @GetMapping(path="/messages")
    public List<Message> listAllMessages(@PathVariable String roomId) {
        Room room = this.roomController.findRoomById(roomId);
        return room.getMessages();
    }

    @CrossOrigin
    @GetMapping(path="/messages/{messageId}")
    public Message findMessage(@PathVariable String roomId, @PathVariable String messageId) {
        Room room = this.roomController.findRoomById(roomId);

        Message foundMessage = null;

        for(Message message : room.getMessages()) {
            if (message.getId().equals(messageId)) {
                foundMessage = message;
            }
        }
        return foundMessage;
    }

    @CrossOrigin
    @PostMapping(path="/messages")
    public Message createMessage(@PathVariable String roomId, @RequestBody Message from) {
        Room room = this.roomController.findRoomById(roomId);

        String messageId = room.getId() + this.coreContext.getAccount().getId() + System.currentTimeMillis() + Math.abs(random.nextInt());

        Message message = new Message();
        message.setContent(from.getContent());
        message.setAttachedFileUrl(from.getAttachedFileUrl());
        message.setId(messageId);
        message.setOwnerId(this.coreContext.getAccount().getId());
        message.setCreatedAt(LocalDateTime.now());

        room.getMessages().add(message);

        this.roomRepository.save(room);

        return message;
    }

    @CrossOrigin
    @PatchMapping(path = "/messages/{messageId}")
    public Message updateMessage(@PathVariable String roomId, @PathVariable String messageId, @RequestBody Message from) {
        Room room = this.roomController.findRoomById(roomId);

        Message modifiedMessage = null;

        for(Message message : room.getMessages()) {
            if (message.getId().equals(messageId)) {
                this.roomController.checkOwnership(message, this.coreContext.getAccount().getId());
                message.setContent(from.getContent());
                modifiedMessage = message;
            }
        }

        if (modifiedMessage == null) {
            throw new NotFoundException();
        }

        this.roomRepository.save(room);

        return modifiedMessage;
    }

    @CrossOrigin
    @DeleteMapping(path = "/messages/{messageId}")
    public void deleteMessage(@PathVariable String roomId, @PathVariable String messageId) {
        Room room = this.roomController.findRoomById(roomId);

        Message messageToDelete = null;

        for(Message message : room.getMessages()) {
            if (message.getId().equals(messageId)) {
                this.roomController.checkOwnership(message, this.coreContext.getAccount().getId());
                messageToDelete = message;
            }
        }

        if (messageToDelete == null) {
            throw new NotFoundException();
        }

        room.getMessages().remove(messageToDelete);

        this.roomRepository.save(room);
    }
}
