import {Component, OnInit} from '@angular/core';
import {DropletService} from "../droplet.service";

@Component({
  selector: 'app-droplet-create',
  templateUrl: './droplet-create.component.html',
  styleUrls: ['./droplet-create.component.css']
})
export class DropletCreateComponent implements OnInit {
  name?: string;
  file?: File;

  constructor(private dropletService: DropletService) {
  }

  ngOnInit(): void {
  }

  onUpload(): void {
    if (this.name && this.file) {
      this.dropletService.upload(this.name, this.file).subscribe(droplet => {
      });
    }
  }

  onFileSelected($event: Event) {
    // @ts-ignore
    this.file = $event.target.files[0];
  }
}
